# Local Kubernetes Cluster Setup for Development

We are using [Multipass](https://canonical.com/multipass) and [MicroK8s](https://microk8s.io/) because they enable us to
**create a multi-node cluster**, allowing us to test our application on **multi-node deployments**. This setup provides
the ability to evaluate **fault tolerance**, **scalability**, and **reliability**—key aspects we need to verify before
deploying to an EKS cluster.

**Note:** This setup assumes you are using [Ubuntu](https://ubuntu.com/) as your operating system. On other operating
systems, you can use tools like [Minikube](https://minikube.sigs.k8s.io), among many others, and follow their simple
setup instructions.

## Prerequisites

- Ubuntu 24.04 LTS (or similar version)
- At least 8 GB RAM and 6 vCPUs available (adjust VM specs if less)
- Internet connection

## Install Multipass

```bash
sudo apt update && sudo apt upgrade -y
sudo snap install multipass
multipass version
```

### Create 3 VMs

```bash
multipass launch -n node1 -c 2 -m 2G -d 10G --cloud-init - <<EOF
#cloud-config
package_update: true
packages:
  - curl
  - apt-transport-https
  - ca-certificates
  - software-properties-common
EOF

multipass launch -n node2 -c 2 -m 2G -d 10G --cloud-init - <<EOF
#cloud-config
package_update: true
packages:
  - curl
  - apt-transport-https
  - ca-certificates
  - software-properties-common
EOF

multipass launch -n node3 -c 2 -m 2G -d 10G --cloud-init - <<EOF
#cloud-config
package_update: true
packages:
  - curl
  - apt-transport-https
  - ca-certificates
  - software-properties-common
EOF
```

**Note:** If a VM fails to start, check its status with multipass list and ensure your system has sufficient resources.

### Install MicroK8s on Each VM

```bash
multipass shell node1
sudo snap install microk8s --classic
sudo usermod -a -G microk8s $USER
sudo chown -f -R $USER ~/.kube
exit
```

Repeat the above steps for `node2` and `node3`:

### Verify MicroK8s Installation

```bash
multipass shell node1
microk8s status --wait-ready
exit
```

Repeat for `node2` and `node3`. Ensure the status shows MicroK8s is running on each node.

## Create the MicroK8s Cluster

```bash
multipass shell node1
microk8s add-node
```

This will output a microk8s join command, e.g.:

```text
microk8s join 10.97.131.27:25000/b6787bcd047139501328b45f733ec945/ad80cfdf6579 --worker
```

Copy this command (the IP and token will be unique to your setup).

### Join node2 and node3 to the cluster:

On node2:

```bash
multipass shell node2
microk8s join 10.97.131.27:25000/b6787bcd047139501328b45f733ec945/ad80cfdf6579 --worker
exit
```

On node3:

```bash
multipass shell node3
microk8s join 10.97.131.27:25000/978e92007c75f9fce0e1db51a26ec021/ad80cfdf6579 --worker
exit
```

## Verify the Cluster

```bash
multipass shell node1
microk8s kubectl get nodes
exit
```

You should see all three nodes listed with a Ready status.

## Configure Access from Your Host

```bash
sudo snap install kubectl --classic
```

Copy the kubeconfig from `node1`:

```bash
multipass shell node1
microk8s config > ~/kubeconfig
exit

# On your local machine
multipass transfer node1:/home/ubuntu/kubeconfig microk8s-kubeconfig
mv microk8s-kubeconfig .kube/
ls -la .kube
```

### Test Cluster Access:

Update the cluster server IP to the node1 IP address:

```bash
multipass list
```

Copy the IP of `node1` (e.g., `10.97.131.27`) and edit `~/.kube/microk8s-kubeconfig` to update the server field:

```yaml
server: https://10.97.131.27:16443
```

Then test access:

```bash
export KUBECONFIG=~/.kube/microk8s-kubeconfig
kubectl get nodes
```

### Enable Useful MicroK8s Addons

```bash
multipass shell node1
microk8s enable dns storage ingress
exit
```

- **dns**: Provides cluster-wide DNS resolution.
- **storage**: Enables persistent storage with a default storage class.
- **ingress**: Allows external access to services via an Ingress controller.

## Cleanup (Optional)

```bash
multipass stop --all
multipass delete --all
multipass purge
```

## Troubleshooting

- **VM not starting:** Check `multipass list` and ensure sufficient CPU/memory (e.g., reduce `-c` or `-m` values).
- **Cluster not forming:** Verify network connectivity between VMs and re-run `microk8s join` if needed.
- **kubectl errors:** Ensure your local `kubectl` version is compatible with MicroK8s (check with
  `microk8s kubectl version`).
- **Still having issues?** Please reach out by opening an issue on GitHub if you encounter any problems with this setup!