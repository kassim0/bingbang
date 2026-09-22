# Idée de nom du projet :
  - pegi island
  - bingbang

key : d4df6345d7fb4a4e842849ef2bf16ba7
https://rawg.io/@kazzmi/apikey

generate openapi: openapi-generator-cli generate -i http://localhost:8080/v3/api-docs -g typescript-angular -o .src/app/openApi

## Structure du repo

- `backend/` : API Spring Boot (voir `backend/pom.xml`)
- `frontend/` : application Angular (voir `frontend/package.json`), anciennement dans le repo séparé `bingbang-front`, fusionné ici avec son historique complet.
