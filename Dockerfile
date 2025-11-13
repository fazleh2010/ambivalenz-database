# Use an official Python runtime as a parent image
FROM python:3.10-slim

# Set the working directory inside the container
WORKDIR /app

# Copy requirements.txt first to leverage Docker caching
COPY requirements.txt .

# Install any needed dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Install any needed dependencies
RUN python -m spacy download de_core_news_sm

# Copy the rest of the application code
COPY . .

# Make port 5000 available to the outside
EXPOSE 5000

# Define environment variable for Flask
ENV FLASK_APP=app.py

# Run the application
CMD ["flask", "run", "--host=0.0.0.0", "--port=5000"]

