FROM amazonlinux:2023@sha256:162fc5b69e11e81023f83a1ab618b184927ff3002d0852432af3b6ae4a1b5304

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
