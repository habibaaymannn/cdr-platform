FROM nginx:alpine

# Copy the static files to Nginx HTML directory
COPY src/main/resources/static /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
