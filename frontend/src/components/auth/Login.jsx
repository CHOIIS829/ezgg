import React, {useEffect, useState} from 'react';
import styled from '@emotion/styled';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import axios from 'axios';

const Login = ({setIsLoggedIn, onLoginSuccess}) => {
    const navigate = useNavigate();
    const location = useLocation();
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });

    const [errors, setErrors] = useState({
        username: '',
        password: '',
    });

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            console.log('이미 로그인 되어 있음, 홈으로 리다이렉트');
            navigate('/');
        }
    }, [navigate]);

    const validateForm = () => {
        let isValid = true;
        const newErrors = {
            username: '',
            password: '',
        };

        if (!formData.username) {
            newErrors.username = '아이디를 입력해주세요';
            isValid = false;
        }

        if (!formData.password) {
            newErrors.password = '비밀번호를 입력해주세요';
            isValid = false;
        }

        setErrors(newErrors);
        return isValid;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            return;
        }
        console.log('로그인 시도:', formData);

        try {
            const response = await axios.post('http://localhost:8888/login', {
                memberUsername: formData.username,
                password: formData.password
            }, {
                withCredentials: true
            });

            console.log('로그인 응답 전체:', response);
            console.log('응답 헤더:', response.headers);

            // 토큰 확인 (Authorization 헤더에서 획득)
            let token = response.headers['authorization'];
            console.log('token : ', token)

            // 토큰이 없는 경우 response.headers에서 대소문자 구분 없이 찾기
            if (!token) {
                const headerNames = Object.keys(response.headers);
                console.log('사용 가능한 헤더:', headerNames);

                for (const header of headerNames) {
                    if (header.toLowerCase() === 'authorization') {
                        token = response.headers[header];
                        console.log('찾은 토큰 헤더:', header, token);
                        break;
                    }
                }
            }

            if (response.status === 200 && token) {
                console.log('로그인 성공, 토큰 저장:', token);

                // Bearer 접두사가 없으면 추가
                if (!token.startsWith('Bearer ')) {
                    token = `Bearer ${token}`;
                }

                localStorage.setItem('token', token);
                console.log('로컬 스토리지에 저장된 토큰:', localStorage.getItem('token'));

                // 로그인 상태 업데이트
                setIsLoggedIn(true);

                // 사용자 정보 가져오기
                if (onLoginSuccess) {
                    console.log('사용자 정보 가져오기 함수 호출');
                    onLoginSuccess();
                }

                // 로그인 성공 후, 원래 가려던 페이지로 리다이렉트 (없으면 홈으로)
                const from = location.state?.from?.pathname || '/';
                console.log('리다이렉트 경로:', from);
                navigate(from, {replace: true});
            } else {
                console.error('로그인 실패: 토큰 없음', response);
                // 응답은 성공이지만 토큰이 없는 경우 확인
                if (response.data && response.data.message) {
                    alert(`로그인에 실패했습니다: ${response.data.message}`);
                } else {
                    alert('로그인에 실패했습니다. 서버 응답에 토큰이 없습니다.');
                }
            }
        } catch (error) {
            console.error('로그인 오류 상세 정보:', error.response || error);

            // 서버에서 오는 오류 메시지 있으면 표시
            if (error.response && error.response.data && error.response.data.message) {
                alert(`로그인 오류: ${error.response.data.message}`);
            } else {
                alert('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
            }
        }
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setErrors(prev => ({
            ...prev,
            [name]: ''
        }));
    };

    return (
        <Container>
            <FormContainer>
                <Title>로그인</Title>
                <Form onSubmit={handleSubmit}>
                    <InputGroup>
                        <Label>아이디</Label>
                        <Input
                            type="text"
                            name="username"
                            placeholder="아이디를 입력하세요"
                            value={formData.username}
                            onChange={handleChange}
                        />
                        {errors.username && <ErrorMessage>{errors.username}</ErrorMessage>}
                    </InputGroup>
                    <InputGroup>
                        <Label>비밀번호</Label>
                        <Input
                            type="password"
                            name="password"
                            placeholder="비밀번호를 입력하세요"
                            value={formData.password}
                            onChange={handleChange}
                        />
                        {errors.password && <ErrorMessage>{errors.password}</ErrorMessage>}
                    </InputGroup>
                    <SubmitButton type="submit">로그인</SubmitButton>
                </Form>
                <JoinLink to="/join">회원가입하기</JoinLink>
            </FormContainer>
        </Container>
    );
};

export default Login;

const Container = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    padding: 2rem;
    background: #0F0F0F;
`;

const FormContainer = styled.div`
    width: 100%;
    max-width: 600px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    padding: 2rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
`;

const Title = styled.h1`
    color: white;
    font-size: 2rem;
    font-weight: 800;
    margin-bottom: 2rem;
    text-align: center;
`;

const Form = styled.form`
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
`;

const InputGroup = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

const Label = styled.label`
    color: white;
    font-size: 0.9rem;
`;

const Input = styled.input`
    padding: 0.8rem;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    border-radius: 4px;
    color: white;
    font-size: 1rem;

    &:focus {
        outline: none;
        background: rgba(255, 255, 255, 0.15);
    }

    &::placeholder {
        color: rgba(255, 255, 255, 0.5);
    }
`;

const ErrorMessage = styled.span`
    color: #FF416C;
    font-size: 0.8rem;
    margin-top: 0.2rem;
`;

const SubmitButton = styled.button`
    padding: 1rem;
    background: #FF416C;
    border: none;
    border-radius: 4px;
    color: white;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: opacity 0.2s;
    opacity: ${props => props.disabled ? 0.5 : 1};
    pointer-events: ${props => props.disabled ? 'none' : 'auto'};

    &:hover {
        opacity: 0.9;
    }
`;

const JoinLink = styled(Link)`
    display: block;
    text-align: center;
    color: #FF416C;
    font-size: 0.9rem;
    text-decoration: none;
    padding: 0.5rem;
    margin-top: 1rem;

    &:hover {
        text-decoration: underline;
    }
`;
