FROM amazonlinux:2023@sha256:bb1c85fe00170acd8a653de5f68cf77c7ec67234a130b18fad7ca1de1f61e3d3

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
