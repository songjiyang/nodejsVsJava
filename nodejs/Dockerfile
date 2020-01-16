FROM nodeshift/centos7-s2i-nodejs:Carbon

# Create app directory
WORKDIR /usr/src/app

USER root

COPY package.json ./

RUN npm set registry https://registry.npm.taobao.org

RUN npm set disturl https://npm.taobao.org/dist

RUN npm i

# Bundle app source
COPY . .

EXPOSE 3000

CMD [ "npm", "run", "pm2docker" ]
