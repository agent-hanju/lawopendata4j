package kr.go.law.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * HTML 파싱 및 정리 유틸리티 클래스
 */
public final class HtmlParserUtil {
    private HtmlParserUtil() {
    }

    /**
     * HTML 문자열에서 순수 텍스트만 추출합니다.
     *
     * @param html HTML 문자열
     * @return 태그가 제거된 순수 텍스트
     */
    public static String toPlainText(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        Document doc = Jsoup.parse(html);
        return doc.text();
    }

    /**
     * HTML을 정리하여 안전한 HTML만 남깁니다.
     * 기본적인 서식 태그(p, br, b, i, u, strong, em 등)만 허용합니다.
     *
     * @param html HTML 문자열
     * @return 정리된 HTML 문자열
     */
    public static String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, Safelist.relaxed());
    }

    /**
     * HTML을 정리하고 텍스트로 변환합니다.
     * 줄바꿈과 공백을 적절히 처리합니다.
     *
     * @param html HTML 문자열
     * @return 정리된 텍스트
     */
    public static String cleanToText(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        Document doc = Jsoup.parse(html);
        // 블록 요소 뒤에 줄바꿈 추가
        doc.select("br").after("\\n");
        doc.select("p").after("\\n\\n");
        doc.select("div").after("\\n");

        String text = doc.text();
        // 연속된 공백 정리
        text = text.replaceAll("\\\\n", "\n");
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("\n ", "\n");
        text = text.replaceAll(" \n", "\n");
        text = text.replaceAll("\n{3,}", "\n\n");
        return text.trim();
    }
}
