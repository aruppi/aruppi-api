# Templeate API ASP.NET core using dotnet 8

Based on https://github.com/insomnyawolf/TemplateApiNet8

Disclaimer, to following doccument is fulled with ideas trying to improve what i found in my previous workplaces, there may be a bit of trauma causing it but i really thing that keeping things simple is the best way to go about solving a problem, if you don't need something just don't use it.

## Highlights & Features

* Simple Rest Api Client & Implementation Example

* ASP.NET Versioned API
  * Authentication & Authorization
  * Classic Controllers
  * OData Controllers
  * Sample Health-Checks at ``/health``
  * Automatic OpenApi Schema Generator (with version support)

* EntityFramework
  * Source Generator Example (It's like reflection but on compile time and with all the type safety features)
  * Scaffolding customization
  * Automatic Soft Deleted Filter

* Docker support

## What can be improved

The following items are part of the examples, and as such they may be good enough but they could be further improved

* Cleanup of unrelated to current tree objects in ef selects
* Finishing the sync logic (Honestly, it's just repeating what i have already done a bunch of times for each related entity)

## Goals & Intentions

The goal of this repo is to create a base experience that can be extended for any possible uses that we may need while keeping simplicity in mind.

The usage is very similar to using ``ASP.NET Core`` directly.

To achieve this I believe that the best way to do it is to keep the application simple, splitting it into (complete) functionalities instead of "layers". If something is worth breaking into its own layer you should probably consider making a package so it can be reused easily between different applications.

## Opinionated defaults

* opt-out basic security => By default every endpoint requires a call that is Authenticated to be used unless you specify that nothing is required via the ``[AllowAnonimous]`` attribute or if you specify certain roles via ``[Authorize(Roles = "Foo, Bar")]``.

* automatic soft-delete filter
* JsonSerializer ReferenceHandler.IgnoreCycles 

## How to run

1. If needed instal [dotnet8 sdk](https://dotnet.microsoft.com/en-us/download/dotnet/8.0)
2. Download the source code
3. Navigate to the source code
4. Double Click either ``start-watch.bat`` or ``start.bat``

## How is the template made

I wanted to keep the ``main`` simple, it does call everything that is used and configured in the app so you can go there to grasp an idea about what's going on, but the implementation of those calls is moved into the ``Startup`` subfolder, there you can find files or folders depending on the item itself and how simple or complex is the thing that it does.

There's also a folder called ``Database`` with all the EntityFramework related things.

The API folder is intended for the exposed functionality of the app.

## Recomendations

### API

Suggested File Structure:
```
+---Shared
|       00BaseController.cs
|       00BaseODataController.cs
|       DtoAnotation.cs
|
+---v1
|   |   ApiVersionAttribute.cs
|   |
|   +---Controllers
|   |       PlaylistController.cs
|   |
|   +---Dto
|   |       Customer.cs
|   |
|   \---OData
|           PlaylistODataController.cs
|
\---v2
    |   ApiVersionAttribute.cs
    |
    +---Controllers
    |       PlaylistController.cs
    |
    +---Dto
    |       Customer.cs
    |
    \---OData
            PlaylistODataController.cs
```

The idea is to keep each version of the API in a different subfolder, and then divide the controllers into OData and classic controllers.

To help with the versions, I created a custom attribute that I later used on the controllers in this version.

## Configuration Example

```jsonc
{
  "Kestrel": {
    "Endpoints": {
      "Http": {
        "Url": "http://localhost:5001"
      }
    }
  },
  "Logging": {
    "LogLevel": {
      "Default": "Information",
      "Microsoft.AspNetCore": "Warning"
    }
  },
  "AllowedHosts": "*",
  "ConnectionStrings": {
    "DatabaseContext": "Data Source=AruppiDb.db;Cache=Shared"
  },
  "AuthenticationAndAuthorization": {
    "JwtOptions": {
      "Authority": "LegitAuthorityHere",
      "ValidAudiences": [
        "VeryLegitAudienceHere"
      ]
    }
  },
  "Swagger": {
    "Generation": {
      // More info at https://swagger.io/docs/specification/authentication/
      // Optional Pick as many as you need/want
      "SecurityConfigs": [
        {
          "Name": "SampleHttpBasic",
          "SecuritySchemeType": "Http",
          "Http": {
            "Scheme": "Basic"
          }
        },
        {
          "Name": "SampleHttpBearer",
          "SecuritySchemeType": "Http",
          "Http": {
            "Scheme": "Bearer"
          }
        },
        {
          "Name": "SampleApiKey",
          "SecuritySchemeType": "ApiKey",
          "ApiKey": {
            "ParameterLocation": "Header",
            "ParameterName": "CustomAuth"
          }
        },
        {
          "Name": "SampleOAuth2",
          "SecuritySchemeType": "OAuth2",
          "Oauth2": {
            "AuthorizationUrl": "https://AuthorizationUrlHere",
            "ApiScopes": {
              "ScopeName": "Scope"
            }
          }
        }
      ],
      // Optional
      "ExtraServers": [
        {
          "Description": "Dev",
          "Url": "http://VeryLegitDevelopmentUrlHere"
        }
      ]
    },
    "Ui": {
      "ClientId": "VeryLegitClientIdHere"
    }
  }
}
```

## Conclusion

### OData

While OData is really flexible and powerful as it lets the user create custom queries as they need, I really wouldn't use it if it's not really needed, as it can expose data to the users making the request that they aren't supposed to be able to access.

### Interfaces

You don't need interfaces for everything, virtual calls are expensive in terms of performance and honestly, most of the time they don't bring value with them unless you are making libraries or using the same processing for several different classes.

Resumed => Use implementation whenever possible, it produces code that runs faster and it's much easier to follow than code through interfaces.

### Repositories using EntityFramework

You don't need to implement a ``repository`` or a ``unit of work`` pattern since EntityFramework already implements them.
Doing so creates overhead for the developer and the application and it also limits the usage of the ``ORM`` which can make certain tasks harder to achieve and even slow down the application by having to search "workarounds" over the imposed limitations.

That doesn't mean you can't extend EntityFramework's database sets if it's really needed, do it by following the example extension methods, you can find it under `PlaylistSetExtensions` class and its usage on `PlaylistController` class in the V2 version of the API.

I also recommend keeping the model restrictions in the model themselves via annotations, this makes them explicit for the developer (checking the model file you can see them directly) and makes you able to use the annotations to validate the models before making any database call (which can also improve performance in the long term)

### Mediator pattern for API endpoint calls

A Mediator pattern should NOT be a thing on a project like this, it degrades performance, makes debugging harder and would be "duplicated" since ASP.NET core already implements a "mediator" pattern on the endpoint routing, it takes all requests on a centric point and routes them to the defined endpoints.

Example With Mediator:

```
HttpRequest1---+                 +-->Controller-----+
               |                 |                  |
HttpRequest2---+                 |                  |           +------>Logic1
               |                 |                  |           |
HttpRequest3---+-->ASP.NET(Core)-+-->Controller-----+->Mediator-+
               |                 |                  |           |
HttpRequest4---+                 |                  |           +------>Logic2
               |                 |                  |
HttpRequest5---+                 +-->Controller-----+
```

Example Without Mediator:

```
HttpRequest1---+                 +-->Controller------+
               |                 |                   |
HttpRequest2---+                 |                 +-+->Logic1
               |                 |                 |
HttpRequest3---+-->ASP.NET(Core)-+-->Controller----+
               |                 |                 |
HttpRequest4---+                 |                 +-+->Logic2
               |                 |                   |
HttpRequest5---+                 +-->Controller------+
```

There's still a central endpoint where all the requests arrive (ASP.NET Core) and they are still properly mapped to what they should do but only once instead of twice therefore you get free performance because the server is doing less work on each call.

### Clean Code

Clean Code/architecture is not about reading a book and blindly following everything it says, it's about making your own (and your coworkers too) future maintaining your code better and easier while also creating a strong and robust program.

In the end, this is a template, feel free to modify it as you wish to make it fit your needs better.

