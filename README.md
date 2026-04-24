# **Aruppi API** (v5.0.0) (Preview release)

> This API has everything about Japan, from anime, music, radio, images, videos ... to japanese culture
>
> These are the services used for the Aruppi App (only available in Spanish language)

![type code](https://img.shields.io/badge/aruppi-API-brightgreen.svg)
![maintenance](https://img.shields.io/badge/maintained-Yes-brightgreen.svg)
![gitrepo](https://img.shields.io/github/stars/aruppi/aruppi-api?style=social)

![Aruppi API Banner](https://raw.githubusercontent.com/aruppi/aruppi-api/v5/assets/cover.png)

Aruppi API has been developed to bring together all the information about Japanese culture, from anime and manga to the most secret places in Japan, its gastronomy and even its festivities.

We are in continuous development to implement more features and improvements in the functioning of it, to later implement it in the mobile application.

# Developer usage

## Prerequisites

- **JDK 21** or higher installed.
- **Gradle** installed (although Ktor can use the Gradle wrapper).
- **Git** for cloning the repository.
- **Docker Compose** installed on your local machine.
  - For an easy installation, consider using **Docker Desktop**, which includes Docker Compose. Download it from [Docker Desktop](https://www.docker.com/products/docker-desktop/).
- A **server** or cloud service to deploy the application (e.g., Heroku, AWS, DigitalOcean).

## Install IntelliJ IDEA Community Edition

Download and install **IntelliJ IDEA Community Edition** from the official JetBrains website: [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download)

**Instructions:**

1. Visit the [IntelliJ IDEA download page](https://www.jetbrains.com/idea/download).
2. Click on the **Download** button under the **Community** edition.
3. Once downloaded, open the installer and follow the on-screen instructions to complete the installation.

### **Set up project**

#### Clone project

```bash
git clone https://github.com/aruppi/aruppi-api.git
cd aruppi-api
```

#### Configure the Environment

Create a `.env` file or set the necessary environment variables for your application, such as:

```
MONGO_CONNECTION_STRING=mongodb://user:password@host:port
MONGO_DATABASE_NAME=aruppi
```

You will be able to find an `example.env` file in the project, simply rename the file by removing ‘example’ and edit the variables

---

### Build the Project

#### Build the Services

Run the following command in the root directory of your project to build the Docker images:

```bash
docker-compose build
```

#### Start the Services

Start the application and the database using Docker Compose:

```bash
docker-compose up -d
```

Explanation:
 - `up`: Creates and starts the containers.
 - `-d`: Runs the containers in detached mode.

#### Verify the Services are Running

To check if the services are running correctly, use:

```bash
docker-compose ps
```

You should see output similar to:

| NAME     | IMAGE          | COMMAND              | SERVICE | CREATED        | STATUS       | PORTS                  |
|----------|----------------|----------------------|---------|----------------|--------------|------------------------|
| ktor_app | aruppi-api-app | "/app/entrypoint.sh" | app     | 19 seconds ago | Up 2 seconds | 0.0.0.0:8080->8080/tcp |

Additionally, you can view the logs to ensure the application has started without errors:

```bash
docker-compose logs -f app
```

## API Documentation

The API documentation is included within the API. If you want to:

- **Consult the endpoints**
- **Perform tests**
- **See the types of responses**

Use these URLs to browse the API docs:

- ReDoc (main): [http://0.0.0.0:8080/api/v5/docs](http://0.0.0.0:8080/api/v5/docs)
- Swagger UI: [http://0.0.0.0:8080/api/v5/swagger](http://0.0.0.0:8080/api/v5/swagger)
- OpenAPI YAML: [http://0.0.0.0:8080/api/v5/openapi.yaml](http://0.0.0.0:8080/api/v5/openapi.yaml)

### GitHub Pages (static docs)

This repository includes a workflow at `.github/workflows/pages-docs.yml` that publishes static docs to GitHub Pages.

1. Go to **Settings > Pages** in GitHub.
2. In **Build and deployment**, select **GitHub Actions** as source.
3. Push to `main` (or run the workflow manually from the Actions tab).

Once deployed, docs are available at:

- ReDoc: `https://<org-or-user>.github.io/<repo>/api/v5/docs/`
- Swagger UI: `https://<org-or-user>.github.io/<repo>/api/v5/swagger/`
- OpenAPI YAML: `https://<org-or-user>.github.io/<repo>/api/v5/openapi.yaml`

> Note: "Try it out" in Swagger requires CORS enabled on your API server for your GitHub Pages domain.

## Countdown to deprecation of v3 API

Aruppi API version 4.x.x has been deprecated, that's why all of you who are using it should migrate as soon as possible to version 5.x.x which we have already released.

Otherwise, if you want to use older versions you can host them yourself on your servers, and download the code in the corresponding branches of [**v2.x.x**](https://github.com/aruppi/aruppi-api/tree/v2) and [**v3.x.x**](https://github.com/aruppi/aruppi-api/tree/v3). In case you want to use an even lower version of the API we recommend you to have a look at this other version [**v1.x.x**](https://github.com/aruppi/aruppi-api-v1)

Currently the Aruppi app on Android is already using the v5.x.x API services from version v3.0.0, which you can download from our website: [**Download Aruppi App**](https://aruppi.jeluchu.com/download)

### **📚 Projects that use the API**

<table>
  <tr>
    <td align="center">
      <a href="https://aruppi.jeluchu.com/">
        <img src="https://avatars2.githubusercontent.com/u/38753425?s=200&v=4" width="75px;" alt="Jeluchu"/><br />
          <sub>
            <b>Aruppi</b>
          </sub>
      </a><br/>
        <sub>Anime y Manga</sub>
      </a>
    </td>
        <td align="center">
      <a href="https://github.com/Fmaldonado6/Akiyama">
        <img src="https://raw.githubusercontent.com/Fmaldonado6/Akiyama/master/images/logo/logo.png" width="75px;" alt="Jeluchu"/><br />
          <sub>
            <b>Akiyama</b>
          </sub>
      </a><br/>
        <sub>Web and App</sub>
      </a>
    </td>
  </tr>
</table>

## Contributors ✨

Here are the **main contributors to the API**, along with Aruppi's creator

<table>
  <tr>
    <td align="center"><a href="https://github.com/Jeluchu"><img src="https://avatars.githubusercontent.com/u/32357592?v=4" width="100px;" alt=""/><br /><sub><b>Jéluchu</b></sub></a><br /><a href="https://www.instagram.com/jeluchu/" title="Instagram">📸</a> <a href="https://about.jeluchu.com/" title="About Jelu">🌍</a> <a href="https://twitter.com/Jeluchu" title="Twitter">📢</a><a href="https://www.linkedin.com/in/jesusmariacalderon/" title="LinkedIn">🔍</a></td>
  </tr>
</table>

There are also **people who have made contributions** and therefore it is also important to highlight them.

<table>
  <tr>
    <td align="center"><a href="https://github.com/Fmaldonado6"><img src="https://avatars.githubusercontent.com/u/28517542?v=4" width="100px;" alt=""/><br /><sub><b>Fmaldonado6</b></sub></a><br/></td>
    <td align="center"><a href="https://github.com/HernanSsj"><img src="https://avatars.githubusercontent.com/u/41026227?v=4" width="100px;" alt=""/><br /><sub><b>HernanSsj</b></sub></a><br/></td>
        <td align="center"><a href="https://github.com/capitanwesler"><img src="https://avatars.githubusercontent.com/u/61250854?v=4" width="100px;" alt=""/><br /><sub><b>Guillermo</b></sub></a><br/></td>
    <td align="center"><a href="https://github.com/Darkangeel-hd"><img src="https://i.pinimg.com/564x/24/73/c0/2473c02e2ac93f617a28b2b5058bb41d.jpg" width="100px;" alt=""/><br /><sub><b>Darkangeel-hd</b></sub></a><br /></td>
  </tr>
</table>

#

### **:busts_in_silhouette: Credits**

- [Darkangeel](https://github.com/Darkangeel-hd) (System administration authority (SYSADM))
- [Jéluchu](https://github.com/Jeluchu) (Multiplatform Developer, designer, and others)
- [Capitanwesler](https://github.com/capitanwesler) (Backend developer, web developer and others)

Copyright © 2022 [Jéluchu](https://about.jeluchu.com/).
