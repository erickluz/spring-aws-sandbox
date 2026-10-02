terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.66.0"
    }
  }
}

provider "aws" {
  region = "sa-east-1"
  access_key = "000008845000"
  secret_key = "teste"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
  s3_use_path_style           = true
  endpoints {
    sts = "http://localhost:4566"
    sqs = "http://localhost:4566"
    sns = "http://localhost:4566"
    s3  = "http://localhost:4566"
  }
}

module "sns_pedido" {
  source = "terraform-aws-modules/sns/aws"
  name = "sns-pedido"

  subscriptions = {
    sqs_notificacao = {
      protocol = "sqs"
      endpoint = module.sqs_notificacao.queue_arn
      # filter_policy = jsonencode({
      #   store = ["notificacao"] 
      # })
    }
    sqs_pedido = {
      protocol = "sqs"
      endpoint = module.sqs_pedido.queue_arn
      # filter_policy = jsonencode({
      #   store = ["pedido"] 
      # })
    }
  }
}

module "sqs_notificacao" {
  source  = "terraform-aws-modules/sqs/aws"
  name = "notificacao-queue"
  tags = {
    Environment = "dev"
  }
}

module "sqs_pedido" {
  source  = "terraform-aws-modules/sqs/aws"
  name = "pedido-queue"
  tags = {
    Environment = "dev"
  }
  create_dlq = true
  redrive_policy = {
    # default is 5 for this module
    maxReceiveCount = 10
  }
}


module "s3_pedido" {
  source = "terraform-aws-modules/s3-bucket/aws"

  bucket = "relatorio-pedido"
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  versioning = {
    enabled = true
  }
}


