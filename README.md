# Sobre

O FIAP CLOUD é um aplicativo para dispositivos Android que permite armazenar arquivos na nuvem[*].
Escrito em Kotlin, utilizando como backend o Pulp e SQLite para armazenamento de credenciais, o FIAP CLOUD faz a comunicação do Android com o Pulp através de chamadas REST API utilizando como client o [Retrofit](https://square.github.io/retrofit/).

[*]: para o desenvolvimento do projeto utilizamos o [pulp-one-container](https://pulpproject.org/pulp-in-one-container/) rodando localmente, mas como o Pulp permite diversas formas de instalação (como [ansible](https://pulpproject.org/ansible-installer/) ou [operator](https://pulpproject.org/pulp-operator/) permitir o acesso dos arquivos do FIAP CLOUD na nuvem é só uma questão de instalar o Pulp em um ambiente que tem acesso remoto (por exemplo, instalar o pulp-operator no [Amazon EKS](aws.amazon.com/eks/) ou [OpenShift](aws.amazon.com/rosa/)


# O que é Pulp?

[Pulp](https://pulpproject.org) é um gerenciador de repositório de código aberto. Ele permite baixar, buscar e armazenar [diversos tipos de conteúo](https://pulpproject.org/content-plugins/) como pacotes maven e python, imagens de container, pacotes rpm, etc.

O projeto FIAP CLOUD utiliza como backend de armazenamento o [Pulp file](https://docs.pulpproject.org/pulp_file/), um dos plugins do Pulp que permite armazenar arquivos (texto, imagens, vídeos, etc).

# Instalação

O procedimento de instalação é feito em duas etapas:
* instalar o Pulp
* clonar, buildar e executar o FIAP CLOUD

### Instalando o Pulp

#### pré-requisito
* Docker instalado

O procedimento a seguir é baseado na [documentação oficial do Pulp](https://pulpproject.org/pulp-in-one-container/).

* usaremos o diretório /tmp para os volumes do container:
```
cd /tmp
mkdir -p pulp/{settings,pulp_storage,pgsql,containers}
cd pulp
echo "CONTENT_ORIGIN='http://$(hostname):8080'
ANSIBLE_API_HOSTNAME='http://$(hostname):8080'
ANSIBLE_CONTENT_HOSTNAME='http://$(hostname):8080/pulp/content'
TOKEN_AUTH_DISABLED=True" >> settings/settings.py
```

* executando o pulp-one-container
```
docker run --detach \
             --publish 8080:80 \
             --name pulp \
             --volume "$(pwd)/settings":/etc/pulp \
             --volume "$(pwd)/pulp_storage":/var/lib/pulp \
             --volume "$(pwd)/pgsql":/var/lib/pgsql \
             --volume "$(pwd)/containers":/var/lib/containers \
             --device /dev/fuse \
             docker.io/pulp/pulp:3.20
```

* alterar a senha do usuário admin
```
docker exec -it pulp bash -c 'pulpcore-manager reset-admin-password'
```


### Instalando o FIAP CLOUD
* clonar o repositório
```
git clone https://github.com/kaory-china/headOverCloud.git
```

* abrir o repositório na IDE
* alterar o arquivo RetrofitFactory.kt ([TODO] mudar essa configuração hardcoded)
 ```
 val URL: String = "http://<ENDEREÇO ONDE PULP FOI INSTALADO>:8080"
 ```
* executar o emulador na IDE (Android Studio) - recomenda-se usar a API 30 e o Pixel 2
* no emulador, permitir que o app tenha acesso aos storage (settings -> Fiap Cloud -> Permissions -> Storage)
* Run app (shift+10)

### Executando

Assim que o programa é instalado, na primeira execução será necessário criar um usuário:
* clique em REGISTRE-SE
* preencha o formulário

Pronto! Agora é só adicionar novos arquivos ...


# Versões suportadas
Todos os testes foram feitos usando:
- Android API 30
- Emulador Pixel 2 (pode funcionar com outros, mas como não foram feitos testes é possível que a interface fique "quebrada")
- Imagem pulp:3.20
