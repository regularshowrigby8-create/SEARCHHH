FROM python:3.11-slim
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
WORKDIR /srv
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt && useradd --create-home searchhh
COPY --chown=searchhh:searchhh searchhh ./searchhh
COPY --chown=searchhh:searchhh config ./config
USER searchhh
EXPOSE 8000
CMD ["uvicorn", "searchhh.api:app", "--host", "0.0.0.0", "--port", "8000"]
