install:
	npm install --no-package-lock --no-audit
	echo "#!/bin/sh\n\tcd portfolio && make format && make lint" > ../.git/hooks/pre-push
	chmod +x ../.git/hooks/pre-push

format: install
	npx prettier --write --ignore-path=target --ignore-path=src/main/webapp/node_modules --ignore-path=src/main/webapp/dist src/main/webapp/**/*.{html,css,js}
	mvn com.coveo:fmt-maven-plugin:format -Dverbose=true

lint: install
	npx eslint --ignore-pattern=**/target --ignore-pattern=**/node_modules --ignore-pattern=**/dist --ignore-pattern=**/webpack.config.js **/*.js
