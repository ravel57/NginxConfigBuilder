[frontend](https://github.com/ravel57/nginx-config-builder)

### Run:
```
sudo docker build --no-cache -t nginx_config_builder . && \
sudo docker run --name nginx_config_builder \
    -d \
    --restart unless-stopped \
    -p 8080:8080 \
    -p 443:443 \
    -p 80:80 \
    --env LOGIN=<YOUR_LOGIN> \
    --env PASSWORD=<YOUR_PASSWORD> \
    --env EMAIL=<YOUR_EMAIL_FOR_CERTBOT> \
    --volume /etc/letsencrypt:/etc/letsencrypt \
    --volume /var/nginx_config_builder:/etc/nginx/ \
    nginx_config_builder
```
