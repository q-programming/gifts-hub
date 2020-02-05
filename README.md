Gifts-hub
=========================================

Latest build : [![CircleCI](https://circleci.com/gh/q-programming/gifts-hub.svg?style=svg)](https://circleci.com/gh/q-programming/gifts-hub)


Gifts hub is a place where you can share your gifts wish list with your friends and family. Every application member
has his own list, to which can easily add, edit or remove gifts which he/she would like to receive.
Other application users can then "claim" gifts , showing other application users (all but gift owner ) that they would like to reailse that gift

##  Accounts

Application accounts can be created either with login password, or login using social media : facebook or google
In case of social media created account, all data like name, surname and photo will be automatically used

## Instalation
Application settings are stored in `application.yml` (please see sample `db/application.yml properties` to have full set of required properties)

Customise following entries : 
* Point to your database `spring.datasource.*`
* Facebook `spring.security.oauth2.client.registration.facebook.clientId` and app `spring.security.oauth2.client.registration.facebook.client.clientSecret` updated with facebook app values 
* Google `spring.security.oauth2.client.registration.google.clientId`  and app `spring.security.oauth2.client.registration.google.clientSecret` updated
* Default `spring.mail.*` mail server information ( can be then  overwrote with databse based parameter via application ) 
* Change `jwt.secret` secret token with some random value
* Default `app.gift.age` for how long gifts are treated as "New" and `gift.newsletter.scheduler` how often newsletter will be sent (cron) (this can be changed via application management as well )

Point to correct properties, using one of methods - order of looking for properties file 
1. Edit context value . For Apache Tomcat 8.x  `context.xml` adding following parameter: 
    `<Parameter name="gifts.properties.path" value="MY_PROPERTY_PATH/application.yml" override="true"/>`
2. Set system property `-Dgifts.properties.path=MY_PROPERTY_PATH/application.yml`
3. If none above is set, package built in properties from `src/main/resources/application.yml` will be used. 
    
Build package (or grab latest artifacts built by CircleCI to use defaults) and deploy to Tomcat container
Create database in your datasource and all tables will be created automatically on first run

First logged in user will be made administrator

Licence
----------

This application was created only be me , if you would like to change something , please notify me . I would love to see it :) 
Whole application is under GNU GPL License and uses some components under Apache License


Please note that application is not unique, there are other application similar in both behaviour and look ( angular material)

Purpose of this application was only for my usage and self development


