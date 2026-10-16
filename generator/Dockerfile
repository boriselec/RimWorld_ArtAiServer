# Image generator (FLUX.2 klein 4B): the local server of the mod, Server/artai.py in RimWorld_ArtAi.
# The Java server calls its POST /generate. Models are downloaded on the first start into /models.
FROM ghcr.io/astral-sh/uv:python3.13-bookworm-slim
WORKDIR /app

# branch, tag or commit of RimWorld_ArtAi to take the server from
ARG ARTAI_REF=main
ADD https://raw.githubusercontent.com/boriselec/RimWorld_ArtAi/${ARTAI_REF}/Server/artai.py artai.py
COPY artai_config.json .

# install the Python libraries into the image, so the container starts without downloading them
RUN uv run artai.py --help

EXPOSE 8081
ENTRYPOINT ["uv", "run", "artai.py"]
