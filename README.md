[frontend](https://github.com/ravel57/nginx-config-builder)

NginxConfigBuilder is a web application for configuring traffic proxying via Nginx to access your network's internal services. It allows you to automate the configuration process, greatly simplifying the management of routes and reverse proxy parameters.

In addition, the application provides the ability to quickly and easily connect SSL certificates using Certbot, ensuring secure encryption of traffic without unnecessary complications.

<img width="425" alt="изображение" src="https://github.com/user-attachments/assets/259d10fd-d512-43ce-ab9d-2ec834580550" />

---

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
