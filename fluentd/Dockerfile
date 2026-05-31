# fluentd 공식 이미지 사용
# 이후 설치 작업은 관리자 권한으로 실행
FROM fluent/fluentd:v1.16-1
USER root

# 빌드 도구 설치 (컴파일 환경 구축)
RUN apk add --no-cache --update \
    build-base \
    ruby-dev \
    zlib-dev \
    cyrus-sasl-dev

# Kafka 플러그인 설치
RUN gem install fluent-plugin-kafka --no-document

# 설치 과정에서 생긴 불필요한 파일 정리
RUN rm -rf /tmp/* /var/cache/apk/* /usr/lib/ruby/gems/*/cache/*.gem

# 다시 일반 사용자로 전환
USER fluent