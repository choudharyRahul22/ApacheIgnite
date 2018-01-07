FROM node:latest
MAINTAINER Rahul Choudhary
ENV CONTAINER_PATH /var/www/socketio
WORKDIR $CONTAINER_PATH
RUN npm install supervisor -g
EXPOSE 3000
ENTRYPOINT ["supervisor","index.js"]