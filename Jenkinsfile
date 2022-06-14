#!/bin/groovy

pipeline {
    agent any
    tools { maven "M3" }

    environment {
        AWS = credentials('AWS-Key')
        AWS_REGION = credentials('AWS-Region')
        SECRET_URL = credentials('AWS-Services-Secret')
        COMMIT_HASH = sh(returnStdout: true, script: "git rev-parse --short=8 HEAD").trim()

        def aws_script = "aws secretsmanager get-secret-value --secret-id $SECRET_URL --region $AWS_REGION"
        def output = sh(returnStdout: true, script: aws_script)
        def repos = readJSON(text: readJSON(text: output).SecretString)

        REPO_HOST = repos["AP-Repo-Host"].toString()
        USERS_REPO = repos["AP-Users-Repo"].toString()
        DOCKER_LOGIN = repos["AP-Docker-Login"].toString()

        MVNW_REPOURL = repos["Maven-Proxy-URL"].toString()
        ARTIFACTORY_PROJECT = repos["Artifactory-Project"].toString()
        ARTIFACTORY_USER = repos["Artifactory-User"].toString()
        ARTIFACTORY_PASSWORD = repos["Artifactory-Password"].toString()
    }

    stages {
        stage('GitHub Fetch') { steps{
            echo(message: 'GitHub Fetch!')
            git(branch: 'dev', url: 'https://github.com/Heads-in-the-Cloud/users-microservice-AP.git')
        }}
        stage('Tests') { steps{
            echo(message: 'Running SonarQube Tests!')
            script{
            withSonarQubeEnv(installationName: 'SonarQube') {
                sh(script: 'mvn -s settings.xml clean verify sonar:sonar -Dsonar.projectKey=Users')
            }}

            timeout(time: 1, unit: 'HOURS') {
                waitForQualityGate(abortPipeline: true)
            }
        }}
        stage('Build') { steps{
            echo(message: 'Building!')
            sh(script: 'mvn -s settings.xml clean package')
            script { image = docker.build("$USERS_REPO:$COMMIT_HASH") }
        }}
        stage('Push to Artifactory') { steps{
            echo(message: 'Deploying!')
            rtUpload(
                serverId: 'ap-jfrog-artifactory',
                spec: '{"files": [{ "pattern": "target/*.jar", "target": "aspms-users/$USERS_REPO/" }]}',
                project: "$ARTIFACTORY_PROJECT"
            )
        }}
        stage('ECR Push') { steps{
            echo(message: 'Pushing!')
            script{
            docker.withRegistry("https://$REPO_HOST", DOCKER_LOGIN) {
                docker.image("$USERS_REPO:$COMMIT_HASH").push()
                docker.image("$USERS_REPO:$COMMIT_HASH").push("latest")
            }}
        }}
        stage('Service Deployment') { steps{
            echo(message: 'Deploying!')

            // Run docker compose up
            echo(message: 'ECS Deploy!')
            build(job: 'ECSDeploy', propagate: true, parameters: [
                booleanParam(name: 'Deploy', value: true ),
                booleanParam(name: 'OnlyRestart', value: true )
            ])

            // Run EKSctl control update pods
            echo(message: 'EKS Deploy!')
            build(job: 'EKSDeploy', propagate: true, parameters: [
                booleanParam(name: 'Deploy', value: true ),
                string( name: 'Service', value: 'users' )
            ])
        }}
    }
    post { always {
        sh(script: 'docker image prune -f -a')
    }}
}