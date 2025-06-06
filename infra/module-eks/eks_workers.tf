resource "aws_iam_role" "worker_role" {
  name = "terraform-eks-demo-node"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "worker_node_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.worker_role.name
}

resource "aws_iam_role_policy_attachment" "cni_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.worker_role.name
}

resource "aws_iam_role_policy_attachment" "ecr_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.worker_role.name
}

resource "aws_iam_role_policy_attachment" "sqs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonSQSFullAccess"
  role       = aws_iam_role.worker_role.name
}

# Autoscaling Full Access
resource "aws_iam_role_policy_attachment" "eks-Autoscaling-Full-Access" {
  policy_arn = "arn:aws:iam::aws:policy/AutoScalingFullAccess"
  role       = aws_iam_role.worker_role.name
}

resource "aws_eks_node_group" "workers_node_group_a" {
  cluster_name    = aws_eks_cluster.sandbox.name
  node_group_name = "${var.cluster_name}-ng-a"
  node_role_arn   = aws_iam_role.worker_role.arn
  subnet_ids      = [aws_subnet.public_subnets[0].id]  # eu-central-1a
  instance_types = ["t2.large"]

  scaling_config {
    desired_size = 1
    max_size     = 2
    min_size     = 1
  }

  depends_on = [
    aws_iam_role_policy_attachment.worker_node_policy,
    aws_iam_role_policy_attachment.cni_policy,
    aws_iam_role_policy_attachment.ecr_policy,
    aws_iam_role_policy_attachment.sqs_policy,
    aws_iam_role_policy_attachment.eks-Autoscaling-Full-Access,
    aws_internet_gateway.igw
  ]

  tags = {
    Name = "Workers-Node-Group-a"
    # Cluster Autoscaler Tags
    "k8s.io/cluster-autoscaler/${var.cluster_name}" = "owned"
    "k8s.io/cluster-autoscaler/enabled" = "TRUE"
  }


}

resource "aws_eks_node_group" "workers_node_group_b" {
  cluster_name    = aws_eks_cluster.sandbox.name
  node_group_name = "${var.cluster_name}-ng-b"
  node_role_arn   = aws_iam_role.worker_role.arn
  subnet_ids      = [aws_subnet.public_subnets[1].id]  # eu-central-1b
  instance_types = ["t2.large"]

  scaling_config {
    desired_size = 1
    max_size     = 2
    min_size     = 1
  }

  depends_on = [
    aws_iam_role_policy_attachment.worker_node_policy,
    aws_iam_role_policy_attachment.cni_policy,
    aws_iam_role_policy_attachment.ecr_policy,
    aws_iam_role_policy_attachment.sqs_policy,
    aws_iam_role_policy_attachment.eks-Autoscaling-Full-Access,
    aws_internet_gateway.igw
  ]

  tags = {
    Name = "Workers-Node-Group-b"
    # Cluster Autoscaler Tags
    "k8s.io/cluster-autoscaler/${var.cluster_name}" = "owned"
    "k8s.io/cluster-autoscaler/enabled" = "TRUE"
  }


}