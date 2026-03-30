# Horizontal Pod Autoscaler policies via Kubernetes Metrics Server
# Req 17.3: CPU threshold 70%, Memory threshold 80%

resource "aws_autoscaling_policy" "cpu_scale_out" {
  name                   = "spartan-gym-${var.env}-cpu-scale-out"
  autoscaling_group_name = aws_eks_node_group.main.resources[0].autoscaling_groups[0].name
  policy_type            = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
    target_value = var.cpu_target_percentage
  }
}

# Kubernetes-level HPA manifest for microservices (applied via kubectl/helm)
# This is the HPA template that should be applied to each microservice deployment
resource "local_file" "hpa_template" {
  filename = "${path.module}/generated/hpa-template.yaml"
  content  = <<-YAML
    # HPA template for Spartan Golden Gym microservices
    # Req 17.3: CPU threshold 70%, Memory threshold 80%
    apiVersion: autoscaling/v2
    kind: HorizontalPodAutoscaler
    metadata:
      name: MICROSERVICE_NAME-hpa
      namespace: spartan-gym-${var.env}
    spec:
      scaleTargetRef:
        apiVersion: apps/v1
        kind: Deployment
        name: MICROSERVICE_NAME
      minReplicas: 2
      maxReplicas: 10
      metrics:
        - type: Resource
          resource:
            name: cpu
            target:
              type: Utilization
              averageUtilization: ${var.cpu_target_percentage}
        - type: Resource
          resource:
            name: memory
            target:
              type: Utilization
              averageUtilization: ${var.memory_target_percentage}
  YAML
}
