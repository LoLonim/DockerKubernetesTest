# Описание приложения
Данное приложение выводит список файлов в папке App на сервере, запущенном из Docker контейнера. Оно было запаковано в Docker контейнер и на его основе был создан pod manifest для kubernetes. Использованы Java spring web, thymeleaf, Docker, minikube.
# Docker
## Шаг 1
Создание Web-приложения
Web-приложение написано на Java Spring web с применением движка шаблонов thymeleaf
Оно считывает все файлы из папки запуска, т.е. при запуске контейнера Docker с рабочей директорией app,
будет показано содержимое app. Порт изменен с 8080 на 8000.
## Шаг 2
Создание Dockerfile
Dockerfile на основе openjdk:17-oracle для корректного запуска приложения. Рабочей директорией выставлена папка app
(предварительно созданная в командах), создан пользователь с UID и GUD 1001, скопирован в образ файл hello.html, в cmd запускается jar файл.      
Dockerfile:
```
FROM openjdk:17-oracle
ARG JAR_FILE=target/testapp-0.0.1-SNAPSHOT.jar
ARG USER=app
ARG UID=1001
ARG GID=1001
RUN useradd ${USER} \
   && usermod -u $UID ${USER} \
   && groupmod -g $GID ${USER} \
   && mkdir -p /app \
   && chown -R ${USER}:${USER} /app
USER ${USER}
COPY --chown=$USER:$USER hello.html /app
WORKDIR /app
EXPOSE 8000
COPY ${JAR_FILE} app.jar  
CMD ["java", "-jar", "app.jar"] 
```
hello.html:
```html
<html><body>
Hello world
</body></html>
```
## Шаг 3
Сборка Docker image
```
$ docker build -t lolonim/web:1.0.0 -t lolonim/web:latest .

Sending build context to Docker daemon  19.55MB
Step 1/12 : FROM openjdk:17-oracle
 ---> 5e28ba2b4cdb
Step 2/12 : ARG JAR_FILE=target/testapp-0.0.1-SNAPSHOT.jar
 ---> Using cache
 ---> e815d9d08c3b
Step 3/12 : ARG USER=app
 ---> Using cache
 ---> 43c3cccf50ce
Step 4/12 : ARG UID=1001
 ---> Using cache
 ---> 7a4821b3d14e
Step 5/12 : ARG GID=1001
 ---> Using cache
 ---> 12dc6f77778a
Step 6/12 : RUN useradd ${USER}    && usermod -u $UID ${USER}    && groupmod -g $GID ${USER}    && mkdir -p /app    && chown -R ${USER}:${USER} /app
 ---> Using cache
 ---> 68f2838117f6
Step 7/12 : USER ${USER}
 ---> Using cache
 ---> 604360cdbeef
Step 8/12 : COPY --chown=$USER:$USER hello.html /app
 ---> Using cache
 ---> c4a8755d88cd
Step 9/12 : WORKDIR /app
 ---> Using cache
 ---> 55bd12dbeca3
Step 10/12 : EXPOSE 8000
 ---> Using cache
 ---> 1e0993395adf
Step 11/12 : COPY ${JAR_FILE} app.jar
 ---> Using cache
 ---> 0292d0a77f92
Step 12/12 : CMD ["java", "-jar", "app.jar"]
 ---> Using cache
 ---> dc567bc1e5c7
Successfully built dc567bc1e5c7
Successfully tagged lolonim/web:1.0.0
Successfully tagged lolonim/web:latest

```
## Шаг 4
Запуск Docker контейнера из Docker image
```
$ docker run -ti --rm -p 8000:8000 lolonim/web:1.0.0

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.0)

2022-06-16 16:17:52.787  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : Starting TestappApplication v0.0.1-SNAPSHOT using Java 17.0.2 on d333a8d1bd50 with PID 1 (/app/app.jar started by app in /app)
2022-06-16 16:17:52.792  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : No active profile set, falling back to 1 default profile: "default"
2022-06-16 16:17:54.313  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8000 (http)
2022-06-16 16:17:54.332  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-06-16 16:17:54.332  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.63]
2022-06-16 16:17:54.463  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-06-16 16:17:54.464  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1578 ms
2022-06-16 16:17:55.050  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8000 (http) with context path ''
2022-06-16 16:17:55.067  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : Started TestappApplication in 2.967 seconds (JVM running for 3.841)
```
Список контейнеров:
```
$ docker container list
CONTAINER ID   IMAGE               COMMAND               CREATED              STATUS              PORTS                                       NAMES
d333a8d1bd50   lolonim/web:1.0.0   "java -jar app.jar"   About a minute ago   Up About a minute   0.0.0.0:8000->8000/tcp, :::8000->8000/tcp   vigilant_shaw
```
## Шаг 5
Авторизация в docker hub и отправка image
Авторизуемся с помощью Access Token
```
$ docker login -u lolonim

Password: 
WARNING! Your password will be stored unencrypted in /home/lolonim/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```
Отправим image в Registry Docker Hub:
```
$ docker push lolonim/web:1.0.0

The push refers to repository [docker.io/lolonim/web]
2e5d9e3f345a: Pushed 
a845d77ba09c: Pushed 
6ffdd4697de5: Pushed 
dc9fa3d8b576: Pushed 
27ee19dc88f2: Pushed 
c8dd97366670: Pushed 
1.0.0: digest: sha256:793285157d8c478eec2cad4ca12046a21e14e0870f02b2fd2fe4d5709b75c211 size: 1581
```
Попробуем скачать image из Docker hub:
```
$ docker pull lolonim/web:1.0.0

1.0.0: Pulling from lolonim/web
Digest: sha256:793285157d8c478eec2cad4ca12046a21e14e0870f02b2fd2fe4d5709b75c211
Status: Image is up to date for lolonim/web:1.0.0
docker.io/lolonim/web:1.0.0
```
Точно такой же image есть в системе, поэтому он не скачивался.
# Kubernetes
## Шаг 1
Запуск кластера
```
$ minikube start

😄  minikube v1.25.2 на Linuxmint 20.3
✨  Используется драйвер docker на основе существующего профиля
👍  Запускается control plane узел minikube в кластере minikube
🚜  Скачивается базовый образ ...
🔄  Перезагружается существующий docker container для "minikube" ...
🐳  Подготавливается Kubernetes v1.23.3 на Docker 20.10.12 ...
    ▪ kubelet.housekeeping-interval=5m
🔎  Компоненты Kubernetes проверяются ...
    ▪ Используется образ gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Включенные дополнения: storage-provisioner, default-storageclass
🏄  Готово! kubectl настроен для использования кластера "minikube" и "default" пространства имён по умолчанию

```
Статус запущенного кластера:
```
$ minikube status

minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```
Конфигурационный файл Kubernetes kubeconfig:
```
$ kubectl config view

apiVersion: v1
clusters:
- cluster:
    certificate-authority: /home/lolonim/.minikube/ca.crt
    extensions:
    - extension:
        last-update: Thu, 16 Jun 2022 19:33:50 MSK
        provider: minikube.sigs.k8s.io
        version: v1.25.2
      name: cluster_info
    server: https://192.168.49.2:8443
  name: minikube
contexts:
- context:
    cluster: minikube
    extensions:
    - extension:
        last-update: Thu, 16 Jun 2022 19:33:50 MSK
        provider: minikube.sigs.k8s.io
        version: v1.25.2
      name: context_info
    namespace: default
    user: minikube
  name: minikube
current-context: minikube
kind: Config
preferences: {}
users:
- name: minikube
  user:
    client-certificate: /home/lolonim/.minikube/profiles/minikube/client.crt
    client-key: /home/lolonim/.minikube/profiles/minikube/client.key
```
Проверим подключение к кластеру:
```
$ kubectl cluster-info

Kubernetes control plane is running at https://192.168.49.2:8443
CoreDNS is running at https://192.168.49.2:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```
Список namespaces:
```
$ kubectl get ns

NAME              STATUS   AGE
default           Active   26h
kube-node-lease   Active   26h
kube-public       Active   26h
kube-system       Active   26h
```
Список объектов кластера:
```
$ kubectl get all -A

NAMESPACE     NAME                                   READY   STATUS    RESTARTS        AGE
default       pod/web                                1/1     Running   2 (7h59m ago)   26h
kube-system   pod/coredns-64897985d-qthg9            1/1     Running   2 (7h59m ago)   26h
kube-system   pod/etcd-minikube                      1/1     Running   2 (7h59m ago)   26h
kube-system   pod/kube-apiserver-minikube            1/1     Running   2 (6m44s ago)   26h
kube-system   pod/kube-controller-manager-minikube   1/1     Running   2 (7h59m ago)   26h
kube-system   pod/kube-proxy-qkt8c                   1/1     Running   2 (7h59m ago)   26h
kube-system   pod/kube-scheduler-minikube            1/1     Running   2 (6m44s ago)   26h
kube-system   pod/storage-provisioner                1/1     Running   5 (5m51s ago)   26h

NAMESPACE     NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                  AGE
default       service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP                  26h
kube-system   service/kube-dns     ClusterIP   10.96.0.10   <none>        53/UDP,53/TCP,9153/TCP   26h

NAMESPACE     NAME                        DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR            AGE
kube-system   daemonset.apps/kube-proxy   1         1         1       1            1           kubernetes.io/os=linux   26h

NAMESPACE     NAME                      READY   UP-TO-DATE   AVAILABLE   AGE
kube-system   deployment.apps/coredns   1/1     1            1           26h

NAMESPACE     NAME                                DESIRED   CURRENT   READY   AGE
kube-system   replicaset.apps/coredns-64897985d   1         1         1       26h
```
## Шаг 2
Создание Pod manifest
Создаем файл pod.yaml с содержимым:
```
apiVersion: v1
kind: Pod
metadata:
  name: web
  labels:
    env: test
spec:
  containers:
  - name: webapp
    image: lolonim/web:1.0.0
    imagePullPolicy: IfNotPresent
```
## Шаг 3
Применим Manifest:
```
$ kubectl apply -f pod.yaml -n default

pod/web created
```
Посмотрим статус во время установки:
```
$ kubectl get pod web -n default --watch

NAME   READY   STATUS    RESTARTS   AGE
web    1/1     Running   0          12s
```
Проверим установку pod (Вывод этой команды также есть в текстовом файле в репозитории):
```
$ kubectl describe pod web -n default

Name:         web
Namespace:    default
Priority:     0
Node:         minikube/192.168.49.2
Start Time:   Thu, 16 Jun 2022 19:43:27 +0300
Labels:       env=test
Annotations:  <none>
Status:       Running
IP:           172.17.0.3
IPs:
  IP:  172.17.0.3
Containers:
  webapp:
    Container ID:   docker://be093846bdaa4ce6670306ab92392eb1861303641599d75faa9072ad80ae31ea
    Image:          lolonim/web:1.0.0
    Image ID:       docker-pullable://lolonim/web@sha256:925004d60304953fcdbbcb717d6e8c00c927b8902dbe44780f9a8505b9dc88ca
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Thu, 16 Jun 2022 19:43:28 +0300
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-6fgm8 (ro)
Conditions:
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True 
Volumes:
  kube-api-access-6fgm8:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  102s  default-scheduler  Successfully assigned default/web to minikube
  Normal  Pulled     102s  kubelet            Container image "lolonim/web:1.0.0" already present on machine
  Normal  Created    102s  kubelet            Created container webapp
  Normal  Started    102s  kubelet            Started container webapp
```
## Шаг 4
Пробросим порт наружу web-приложения:
```
$ kubectl port-forward pods/web 8000:8000

Forwarding from 127.0.0.1:8000 -> 8000
Forwarding from [::1]:8000 -> 8000
```
В новой консоли проверим работоспособность и доступность web-приложения:
```html
$ curl http://127.0.0.1:8000/hello.html

<html><body>
Hello world
</body></html>
```
Логи внутри контейнера:
```
$ kubectl logs web -n default


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.0)

2022-06-16 16:43:30.239  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : Starting TestappApplication v0.0.1-SNAPSHOT using Java 17.0.2 on web with PID 1 (/app/app.jar started by app in /app)
2022-06-16 16:43:30.242  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : No active profile set, falling back to 1 default profile: "default"
2022-06-16 16:43:32.064  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8000 (http)
2022-06-16 16:43:32.077  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-06-16 16:43:32.078  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.63]
2022-06-16 16:43:32.194  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-06-16 16:43:32.194  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1855 ms
2022-06-16 16:43:33.149  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8000 (http) with context path ''
2022-06-16 16:43:33.163  INFO 1 --- [           main] com.nexign.testapp.TestappApplication    : Started TestappApplication in 3.773 seconds (JVM running for 4.577)
2022-06-16 16:47:03.067  INFO 1 --- [nio-8000-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2022-06-16 16:47:03.068  INFO 1 --- [nio-8000-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2022-06-16 16:47:03.069  INFO 1 --- [nio-8000-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
```
Удалим pod:
```
$ kubectl delete pod web

pod "web" deleted
```
Остановим minikube:
```
$ minikube stop

✋  Узел "minikube" останавливается ...
🛑  Выключается "minikube" через SSH ...
🛑  Остановлено узлов: 1.
```
