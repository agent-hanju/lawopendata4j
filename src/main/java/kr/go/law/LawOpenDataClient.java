package kr.go.law;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// import kr.go.law.administrative.api.AdministrativeApiClient;
// import kr.go.law.committee.api.CommitteeApiClient;
import kr.go.law.config.LawOpenDataProperties;
// import kr.go.law.constitutional.api.ConstitutionalApiClient;
// import kr.go.law.interpretation.api.InterpretationApiClient;
import kr.go.law.precedent.api.PrecedentApiClient;
import kr.go.law.statute.api.StatuteApiClient;
// import kr.go.law.term.api.TermApiClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Law Open Data API 통합 클라이언트
 * <p>
 * 법령, 판례 정보를 조회하는 단일 진입점 클라이언트입니다.
 * </p>
 *
 * <pre>
 * // 기본 사용
 * LawOpenDataClient client = LawOpenDataClient.builder()
 *     .oc("YOUR_API_KEY")
 *     .build();
 *
 * // 법령 API 사용
 * client.statute().callListApi(...);
 *
 * // 판례 API 사용
 * client.precedent().callListApi(...);
 * </pre>
 */
@Slf4j
public class LawOpenDataClient {

    private final LawOpenDataProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    @Getter
    private final StatuteApiClient statute;

    @Getter
    private final PrecedentApiClient precedent;

    // @Getter
    // private final TermApiClient term;

    // @Getter
    // private final ConstitutionalApiClient constitutional;

    // @Getter
    // private final InterpretationApiClient interpretation;

    // @Getter
    // private final AdministrativeApiClient administrative;

    // @Getter
    // private final CommitteeApiClient committee;

    /**
     * LawOpenDataClient 생성자
     *
     * @param properties   API 설정 프로퍼티
     * @param objectMapper Jackson ObjectMapper
     * @param okHttpClient OkHttp 클라이언트
     */
    public LawOpenDataClient(LawOpenDataProperties properties, ObjectMapper objectMapper,
            OkHttpClient okHttpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.okHttpClient = okHttpClient;
        this.statute = new StatuteApiClient(properties, objectMapper, okHttpClient);
        this.precedent = new PrecedentApiClient(properties, objectMapper, okHttpClient);
        // this.term = new TermApiClient(properties, objectMapper, okHttpClient);
        // this.constitutional = new ConstitutionalApiClient(properties, objectMapper, okHttpClient);
        // this.interpretation = new InterpretationApiClient(properties, objectMapper, okHttpClient);
        // this.administrative = new AdministrativeApiClient(properties, objectMapper, okHttpClient);
        // this.committee = new CommitteeApiClient(properties, objectMapper, okHttpClient);
    }

    /**
     * LawOpenDataClient 생성자 (ObjectMapper 및 OkHttpClient 자동 생성)
     *
     * @param properties API 설정 프로퍼티
     */
    public LawOpenDataClient(LawOpenDataProperties properties) {
        this(properties, createDefaultObjectMapper(), createOkHttpClient(properties));
    }

    /**
     * LawOpenDataClient 생성자 (API 키만 사용, 기본 설정 적용)
     *
     * @param oc API 키 (OC)
     */
    public LawOpenDataClient(String oc) {
        this(LawOpenDataProperties.builder().oc(oc).build());
    }

    /**
     * 기본 ObjectMapper 생성
     *
     * @return ObjectMapper
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

        return mapper;
    }

    /**
     * OkHttpClient 생성
     *
     * @param properties API 설정 프로퍼티
     * @return OkHttpClient
     */
    private static OkHttpClient createOkHttpClient(LawOpenDataProperties properties) {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(
                        properties.getMaxConnection(),
                        properties.getKeepAliveDuration().toMillis(),
                        TimeUnit.MILLISECONDS))
                .addInterceptor(new RetryInterceptor(properties.getMaxRetries(), properties.getRetryDelay()))
                .connectTimeout(properties.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    /**
     * Builder 패턴으로 클라이언트 생성
     *
     * @return LawOpenDataClientBuilder
     */
    public static LawOpenDataClientBuilder builder() {
        return new LawOpenDataClientBuilder();
    }

    /**
     * LawOpenDataClient Builder
     */
    public static class LawOpenDataClientBuilder {
        private final LawOpenDataProperties.LawOpenDataPropertiesBuilder propertiesBuilder = LawOpenDataProperties
                .builder();

        private ObjectMapper objectMapper;
        private OkHttpClient okHttpClient;

        /**
         * API 키 설정
         *
         * @param oc API 키
         * @return Builder
         */
        public LawOpenDataClientBuilder oc(String oc) {
            propertiesBuilder.oc(oc);
            return this;
        }

        /**
         * 연결 타임아웃 설정
         *
         * @param connectionTimeout 연결 타임아웃
         * @return Builder
         */
        public LawOpenDataClientBuilder connectionTimeout(java.time.Duration connectionTimeout) {
            propertiesBuilder.connectionTimeout(connectionTimeout);
            return this;
        }

        /**
         * 읽기 타임아웃 설정
         *
         * @param readTimeout 읽기 타임아웃
         * @return Builder
         */
        public LawOpenDataClientBuilder readTimeout(java.time.Duration readTimeout) {
            propertiesBuilder.readTimeout(readTimeout);
            return this;
        }

        /**
         * 최대 재시도 횟수 설정
         *
         * @param maxRetries 최대 재시도 횟수
         * @return Builder
         */
        public LawOpenDataClientBuilder maxRetries(int maxRetries) {
            propertiesBuilder.maxRetries(maxRetries);
            return this;
        }

        /**
         * 재시도 대기 시간 설정
         *
         * @param retryDelay 재시도 대기 시간
         * @return Builder
         */
        public LawOpenDataClientBuilder retryDelay(java.time.Duration retryDelay) {
            propertiesBuilder.retryDelay(retryDelay);
            return this;
        }

        /**
         * 최대 연결 수 설정
         *
         * @param maxConnection 최대 연결 수
         * @return Builder
         */
        public LawOpenDataClientBuilder maxConnection(int maxConnection) {
            propertiesBuilder.maxConnection(maxConnection);
            return this;
        }

        /**
         * Keep-Alive 지속 시간 설정
         *
         * @param keepAliveDuration Keep-Alive 지속 시간
         * @return Builder
         */
        public LawOpenDataClientBuilder keepAliveDuration(java.time.Duration keepAliveDuration) {
            propertiesBuilder.keepAliveDuration(keepAliveDuration);
            return this;
        }

        /**
         * 커스텀 ObjectMapper 설정
         *
         * @param objectMapper Jackson ObjectMapper
         * @return Builder
         */
        public LawOpenDataClientBuilder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * 커스텀 OkHttpClient 설정
         *
         * @param okHttpClient OkHttp 클라이언트
         * @return Builder
         */
        public LawOpenDataClientBuilder okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * LawOpenDataClient 생성
         *
         * @return LawOpenDataClient
         */
        public LawOpenDataClient build() {
            LawOpenDataProperties properties = propertiesBuilder.build();

            if (objectMapper == null) {
                objectMapper = createDefaultObjectMapper();
            }

            if (okHttpClient == null) {
                okHttpClient = createOkHttpClient(properties);
            }

            return new LawOpenDataClient(properties, objectMapper, okHttpClient);
        }
    }

    /**
     * 리트라이 로직용 OkHttpClient 인터셉터
     *
     * 재시도하는 경우:
     * - 서버 에러 (5xx status codes)
     * - 요청 제한 발생 (429 status code)
     * - 네트워크 오류 (IOException)
     */
    @Slf4j
    private static class RetryInterceptor implements Interceptor {

        private final int maxRetries;
        private final Duration retryDelay;

        /**
         * RetryInterceptor 생성자
         *
         * @param maxRetries 최대 재시도 횟수
         * @param retryDelay 재시도 대기 시간
         */
        public RetryInterceptor(int maxRetries, Duration retryDelay) {
            this.maxRetries = maxRetries;
            this.retryDelay = retryDelay;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            IOException lastException = null;
            int attempt = 0;

            while (attempt <= maxRetries) {
                attempt++;

                try {
                    Response response = chain.proceed(request);
                    int statusCode = response.code();

                    if (response.isSuccessful() || (statusCode < 500 && statusCode != 429)) {
                        return response;
                    } else {
                        log.warn("Request failed with status {}, attempt {}/{}: {}",
                                statusCode, attempt, maxRetries + 1, request.url());

                        response.close();

                        if (attempt <= maxRetries) {
                            Duration properDelay = (statusCode == 429)
                                    ? retryDelay.multipliedBy(2)
                                    : retryDelay;
                            waitBeforeRetry(attempt, properDelay);
                        } else {
                            throw new IOException(String.format(
                                    "Request failed after %d attempts with status %d: %s",
                                    maxRetries + 1, statusCode, request.url()));
                        }
                    }

                } catch (IOException e) {
                    lastException = e;
                    log.warn("Request failed with IOException, attempt {}/{}: {} - {}",
                            attempt, maxRetries + 1, request.url(), e.getMessage());

                    if (attempt <= maxRetries) {
                        waitBeforeRetry(attempt, retryDelay);
                    } else {
                        throw new IOException(String.format(
                                "Request failed after %d attempts: %s",
                                maxRetries + 1, request.url()), e);
                    }
                }
            }
            throw lastException == null
                    ? new IOException("Request failed after " + (maxRetries + 1) + " attempts")
                    : lastException;
        }

        private void waitBeforeRetry(int attempt, Duration delay) {
            long waitMs = delay.toMillis() * attempt;

            log.debug("Waiting {}ms before retry attempt {}", waitMs, attempt + 1);

            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Retry wait interrupted", e);
            }
        }
    }

    /**
     * API 설정 프로퍼티 반환
     *
     * @return LawOpenDataProperties
     */
    public LawOpenDataProperties getProperties() {
        return properties;
    }

    /**
     * ObjectMapper 반환
     *
     * @return ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * OkHttpClient 반환
     *
     * @return OkHttpClient
     */
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
