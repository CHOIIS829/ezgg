package com.matching.ezgg.domain.memberInfo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.matching.ezgg.domain.memberInfo.entity.MemberInfo;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {

	@Query("SELECT m.puuid FROM MemberInfo m WHERE m.memberId = :memberId")
	Optional<String> findPuuidByMemberId(Long memberId);

	@Query("SELECT m.memberId FROM MemberInfo m WHERE m.puuid = :puuid")
	Optional<Long> findMemberIdByPuuid(String puuid);

	Optional<MemberInfo> findByPuuid(String puuid);

	Optional<MemberInfo> findByMemberId(Long memberId);

	// riotUsername과 riotTag를 동시에 일치하는 회원이 존재하는 지 확인
	boolean existsByRiotUsernameAndRiotTag(String riotUsename, String riotTag);
}
