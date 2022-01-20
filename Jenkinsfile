#!/bin/groovy

pipeline {
    agent any

    environment {
        AWS = credentials('AWS-Key')

        def aws_script = "aws secretsmanager get-secret-value --secret-id prod/Angel/Secrets --region us-east-2"
        def output = sh(returnStdout: true, script: aws_script)
        def repos = readJSON(text: readJSON(text: output).SecretString)

        users_repo = repos["AP-Users-Repo"].toString()
    }

    stages {
        stage('GitHub Fetch') { steps{
            echo(message: 'GitHub Fetch!')
            git(branch: 'dev', url: 'https://github.com/Heads-in-the-Cloud/users-microservice-AP.git')
        }}
        stage('Tests') { steps{
            echo(message: 'Testing!')
        }}
        stage('Build') { steps{
            echo(message: 'Building!')
            sh(script: 'mvn clean package')
            image = docker.build "ap-users:latest"
        }}
        stage('Archive artifacts and Deployment') { steps{
            echo(message: 'Deploying!')
            archiveArtifacts(artifacts: 'target/*.jar')

            docker.withRegistry(users_repo, "ecr:us-east-2:AWS-Key") {
                docker.image("latest").push()
            }
        }}
    }
}