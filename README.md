# Project_network

## Installation

1. **Prérequis** :
    - Java 11 ou supérieur installé
    - Git (optionnel, pour cloner le dépôt)

2. **Clonage du projet** :
    ```bash
    git clone https://github.com/bolobos/Project_network.git
    cd Project_network
    ```

3. **Compilation** :
    ```bash
    javac -d bin src/*.java
    ```

4. **Exécution** :
    - Pour lancer un serveur :
      ```bash
      java -cp bin Serveur
      ```
    - Pour lancer un client :
      ```bash
      java -cp bin Client
      ```

---

## Utilisation

- **Démarrer un serveur** : Exécutez la commande du serveur sur la machine de votre choix.
- **Connecter un client** : Lancez un client et saisissez un nom unique lors de la connexion.
- **Envoyer un message** : Depuis un client, spécifiez le nom du destinataire et le message à envoyer.

---

## Exemple de scénario

1. Lancez deux serveurs sur des machines différentes.
2. Connectez plusieurs clients à chaque serveur.
3. Envoyez des messages entre clients connectés à différents serveurs : le routage dynamique transmettra les messages automatiquement.

---