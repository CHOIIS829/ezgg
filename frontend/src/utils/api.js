import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8888',
  timeout: 10000,
  withCredentials: true,
});

// 토큰 관련 유틸리티 함수들
const tokenUtils = {
  get: () => localStorage.getItem('token'),
  set: (token) => localStorage.setItem('token', 'Bearer ' + token),
  remove: () => localStorage.removeItem('token'),
  format: (token) => token.startsWith('Bearer ') ? token : `Bearer ${token}`,
};

// 토큰 리프레시 함수
const refreshToken = async () => {
  try {
    console.log('토큰 리프레시 시도');
    const { headers: { authorization: newToken } } = await axios.post(
      'http://localhost:8888/refresh',
      {},
      {
        withCredentials: true,
        headers: { 'Content-Type': 'application/json' }
      }
    );

    if (!newToken) throw new Error('새 토큰을 받지 못했습니다.');
    tokenUtils.set(newToken);
    return newToken;
  } catch (error) {
    tokenUtils.remove();
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/login';
    }
    throw error;
  }
};

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = tokenUtils.get();
    if (token) {
      config.headers['Authorization'] = tokenUtils.format(token);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    const token = response.headers['authorization'];
    if (token) tokenUtils.set(token);
    return response;
  },
  async (error) => {
    console.log('error : ', error.response.data.message);
    const status = error.response?.status;
    const message = error.response?.data?.message;
    const isTokenExpired = status === 401 || status === 403;

    // 토큰이 만료된 경우에만 재발급 시도
    const shouldRefresh =
      isTokenExpired &&
      message === 'token is expired' &&
      !error.config._retry &&
      error.config.url !== '/refresh';

    // 토큰 재발급 시도
    if (shouldRefresh) {
      try {
        error.config._retry = true;
        error.config.headers['Authorization'] = await refreshToken();
        return api(error.config);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    // 401 또는 403인데 token is expired가 아닌 경우 → 로그아웃 + 로그인 이동
    if (status === 401 || status === 403) {
      try {
        await axios.post(
          'http://localhost:8888/auth/logout',
          {},
          {
            headers: { authorization: tokenUtils.get() },
            withCredentials: true,
          }
        );
      } catch (logoutError) {
        console.error('로그아웃 처리 중 오류 발생:', logoutError);
      } finally {
        tokenUtils.remove();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error); 
  }
);

export { tokenUtils };
export default api;