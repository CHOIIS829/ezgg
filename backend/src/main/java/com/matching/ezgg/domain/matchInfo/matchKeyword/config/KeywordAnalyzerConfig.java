package com.matching.ezgg.domain.matchInfo.matchKeyword.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.matching.ezgg.domain.matchInfo.matchKeyword.analyzer.KeywordAnalyzer;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.GlobalMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.GlobalKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.JugKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.LanerKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.keyword.SupKeyword;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.KeywordRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.ClutchWinnerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.ComebackWinRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.CooperativeRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.GoodSynergyRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.HighDamageRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.HighKdaRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.HighKillParticipationRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.InvadingRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.LevelDiffRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.NeverGivesUpRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.SurvivabilityRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.globalRule.TeamFighterRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.CounterJunglerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.JugFirstBloodMakerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.JugVisionDominaceRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.JungleDominanceRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.ObjectiveStealerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.ObjectiveTakerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.jugRule.RiftHeraldUtilizerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.AssassinRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.CsAdvantageRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.EarlyRoamRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.LaneDominanceRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.LanerFirstBloodMakerRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.TurretDefenseRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.TurretDiveRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.lanerRule.TurretKillsRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule.AssistKingRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule.SaveAllyRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule.SupVisionDominanceRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.rule.supRule.WardsPlacedRule;
import com.matching.ezgg.domain.matchInfo.matchKeyword.service.KeywordService;

@Configuration
public class KeywordAnalyzerConfig {

	/**
	 * keyword 종류별 Keyword Rule 리스트
	 */

	@Bean("globalRules")
	public List<KeywordRule<GlobalMatchParsingDto, GlobalKeyword>> globalKeywordRules() {
		return List.of(
			new HighKillParticipationRule(),
			new HighKdaRule(),
			new LevelDiffRule(),
			new GoodSynergyRule(),
			new NeverGivesUpRule(),
			new TeamFighterRule(),
			new CooperativeRule(),
			new InvadingRule(),
			new ComebackWinRule(),
			new HighDamageRule(),
			new ClutchWinnerRule(),
			new SurvivabilityRule()
		);
	}

	@Bean("lanerRules")
	public List<KeywordRule<LanerMatchParsingDto, LanerKeyword>> lanerKeywordRules() {
		return List.of(
			new AssassinRule(),
			new CsAdvantageRule(),
			new EarlyRoamRule(),
			new LanerFirstBloodMakerRule(),
			new LaneDominanceRule(),
			new TurretDefenseRule(),
			new TurretDiveRule(),
			new TurretKillsRule()
		);
	}

	@Bean("jugRules")
	public List<KeywordRule<JugMatchParsingDto, JugKeyword>> jugKeywordRules() {
		return List.of(
			new CounterJunglerRule(),
			new JugFirstBloodMakerRule(),
			new JungleDominanceRule(),
			new ObjectiveStealerRule(),
			new ObjectiveTakerRule(),
			new RiftHeraldUtilizerRule(),
			new JugVisionDominaceRule()
		);
	}

	@Bean("supRules")
	public List<KeywordRule<SupMatchParsingDto, SupKeyword>> supKeywordRules() {
		return List.of(
			new AssistKingRule(),
			new SaveAllyRule(),
			new SupVisionDominanceRule(),
			new WardsPlacedRule()
		);
	}

	/**
	 * keyword 종류별 Analyzer
	 */

	@Bean("globalKeywordAnalyzer")
	public KeywordAnalyzer<GlobalMatchParsingDto, GlobalKeyword> globalKeywordAnalyzer(
		KeywordService keywordService,
		@Qualifier("globalRules") List<KeywordRule<GlobalMatchParsingDto, GlobalKeyword>> globalRules) {
		return new KeywordAnalyzer<>(keywordService, globalRules);
	}

	@Bean("lanerKeywordAnalyzer")
	public KeywordAnalyzer<LanerMatchParsingDto, LanerKeyword> lanerKeywordAnalyzer(
		KeywordService keywordService,
		@Qualifier("lanerRules") List<KeywordRule<LanerMatchParsingDto, LanerKeyword>> lanerRules) {
		return new KeywordAnalyzer<>(keywordService, lanerRules);
	}

	@Bean("jugKeywordAnalyzer")
	public KeywordAnalyzer<JugMatchParsingDto, JugKeyword> jugKeywordAnalyzer(
		KeywordService keywordService,
		@Qualifier("jugRules") List<KeywordRule<JugMatchParsingDto, JugKeyword>> jugRules) {
		return new KeywordAnalyzer<>(keywordService, jugRules);
	}

	@Bean("supKeywordAnalyzer")
	public KeywordAnalyzer<SupMatchParsingDto, SupKeyword> supKeywordAnalyzer(
		KeywordService keywordService,
		@Qualifier("supRules") List<KeywordRule<SupMatchParsingDto, SupKeyword>> supRules) {
		return new KeywordAnalyzer<>(keywordService, supRules);
	}

}
