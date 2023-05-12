# OpenAI API server

## Package

```shell
mvn clean package -U -Dmaven.test.skip=true 
```

## Run

```shell
java -jar openai-api-server-0.1.0.jar -server.port=8080 -server.address=127.0.0.1
```

## Environment variables

### OpenAI

- OPENAI_API_KEY: OpenAI api key
- OPENAI_MODEL: which modal is used, default 'gpt-3.5-turbo'
- OPENAI_TEMPERATURE: OpenAI temperature, default 1
- OPENAI_MAX_TOKENS: max tokens, default 2048
- OPENAI_REDUCE_TOKEN: remove completions in context, default false
- OPENAI_TIMEOUT_CONNECT; connect timeout when request OpenAI API, default 180 seconds
- OPENAI_TIMEOUT_READ: read timeout when request OpenAI API, default 180 seconds
- OPENAI_TIMEOUT_WRITE: write timeout when request OpenAI API, default 180 seconds
- OPENAI_SERVER_SCHEMA: OpenAI API schma, default 'https'
- OPENAI_SERVER_HOST: OpenAI API host, default 'api.openai.com'
- OPENAI_SERVER_PORT: OpenAI API port, default 443
- OPENAI_SERVER_BASE_PATH: OpenAI API context path, default 'v1'
- OPENAI_PROXY_TYPE: set proxy type, http, https or socket ..,
- OPENAI_PROXY_HOST: proxy host
- =OPENAI_PROXY_PORT: proxy port
- OPENAI_PROXY_USER: proxy authentication username
- OPENAI_PROXY_PASS: proxy authentication password
- OPENAI_CHAT_TITLE_PROMPT: prompt for summarizing conversation, default 'Please generate a four to five word title summarizing our conversation without any lead-in, punctuation, quotation marks, periods, symbols, or additional text. Remove enclosing quotation marks.'
- OPENAI_DEFAULT_PROMPT: default prompt, default none

### App

- SERVER_URL: URL of your server, default: 'http://localhost:8080'
- APP_TITLE: Web title, default 'ChatGPT'
- APP_VERSION: app version, default 'Beta'
- ADMIN_USER: initialized administrator name, default 'admin'
- ADMIN_PASS: initialized administrator password, default none, means auto generation
- TOKEN_EXPIRE_DAYS: token expire days, default 10
- TOKEN_AES_KEY: token encrypt key, default 'WDa5x1+/y3NagFX22jcUsQ=='
- APP_USE_WATERMARK: use watermark, default false


