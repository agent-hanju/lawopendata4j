package kr.go.law.committee.api;

import lombok.Getter;

/**
 * 위원회 결정문 타입
 */
@Getter
public enum CommitteeType {
    /**
     * 개인정보보호위원회
     */
    PPC("ppc", "개인정보보호위원회"),

    /**
     * 공정거래위원회
     */
    FTC("ftc", "공정거래위원회"),

    /**
     * 노동위원회
     */
    NLRC("nlrc", "노동위원회"),

    /**
     * 국가인권위원회
     */
    NHRCK("nhrck", "국가인권위원회");

    private final String target;
    private final String name;

    CommitteeType(String target, String name) {
        this.target = target;
        this.name = name;
    }
}
