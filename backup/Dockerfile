# amazonlinux does not provide packages for postgresql16 as of now
# https://github.com/amazonlinux/amazon-linux-2023/issues/516
FROM public.ecr.aws/docker/library/postgres:16 as pg-client
FROM amazonlinux:2023

RUN dnf update -y \
  && dnf install -y libpq awscli jq \
  && dnf clean all

COPY --from=pg-client /usr/lib/postgresql/16 /usr/lib/postgresql/16
ENV LD_LIBRARY_PATH="/usr/lib/postgresql/16/lib:${LD_LIBRARY_PATH}"
ENV PATH="/usr/lib/postgresql/16/bin:${PATH}"

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
