FROM amazonlinux:2023

RUN yum update && yum install -y postgresql15 awscli

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
