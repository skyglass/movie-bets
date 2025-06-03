kubectl delete ingress ingress-service
kubectl delete service moviebets-postgres
kubectl delete service kafka-ui
helm uninstall cert-manager --namespace public-ingress
helm uninstall ingress-nginx --namespace public-ingress
kubectl delete deployment keycloak-postgres
kubectl delete kafkanodepool kafka
kubectl delete kafka my-cluster
terraform destroy --auto-approve