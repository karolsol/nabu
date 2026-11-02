# Development
## Stack
Backend:

- Gradle
- Spring boot
- PostgreSQL

Frontend:

- vite
- Vue.js
- Tanstack
## Requirements
- Gradle and java with version similar to the one used in the project (check .tool-versions file)
- PostgreSQL database(or docker to run it)
- A modern web browser

## Installing dependencies
You can do it with your package manager of choice, but I recommend asdf:
```bash
asdf add plugin gradle
asdf add plugin java
asdf install
asdf reshim
```

## Spotless
To configure format for your favourite IDE use official guide [here](https://github.com/google/google-java-format).
To check formatting run:
```bash
gradle spotlessCheck
```

To fix formatting run:
```bash
gradle spotlessApply
```

## Tests
To run tests, you can use the following command:
```bash
gradle test
```
## API documentation
You can find the API documentation in the /swagger-ui route of the app. It is generated automatically from the code, so it is always up to date.
