## Instrukcja wdrożenia backendu

### Wstęp

Architektura backendu składa się z następujących części:
 - Serwer aplikacyjny (Spring Boot)
 - Serwer uwierzytelniający (Keycloak)
 - Serwer bazodanowy (PostgreSQL z rozszerzeniem PostGIS, uruchomiony w usłudze Amazon RDS)
 - Usługa przechowywania plików w chmurze (Amazon S3)
 - Usługa CDN (Amazon CloudFront)
 - API dające dostęp dużego modelu językowego, kompatybilnego z OpenAI API (Używany jest Groq API)
 - Usługa FirebaseCloudMessaging do wysyłania powiadomień push

W związku ze stosunkowo skomplikowaną architekturą, zdecydowano się na wykorzystanie narzędzi
ułatwiających proces wdrożenia. Zautomatyzowano proces tworzenia architektury chmurowej za pomocą Terraform.
Użyto również Docker oraz Docker Compose do zautomatyzowania procesu budowania i uruchamiania kontenerów.
Pozwoliło to w znacznym stopniu zautomatyzować proces wdrożenia oraz zarządzania infrastrukturą.

Warto jednak zaznaczyć, że wdrożenie nie jest w pełni zautomatyzowane. Manualny charakter
niektórych kroków (np. konfiguracja Keycloak) sprawia, że wdrożenie wymaga pewnej wiedzy
z zakresu zarządzania infrastrukturą oraz konfiguracji serwerów. Poniżej znajduje się
szczegółowa instrukcja wdrożenia.


### Wymagania wstępne
 - Konto w usłudze Amazon Web Services, posiadające dostęp do usług RDS, S3, CloudFront, EC2, IAM
 - Zainstalowane narzędzie Terraform, posiadające dostęp do powyższego konta (poprzez plik credentials)
 - Dostęp do dwóch nazw domenowych, z możliwością konfiguracji rekordów DNS
 - Dostęp do API OpenAI, Groq lub innej kompatybilnej z OpenAI API
 - Dostęp do projektu Firebase aplikacji mobilnej, z dostępem do Firebase Cloud Messaging
 - Klucz prywatny i publiczny używany do dostępu do maszyn EC2
 - Zainstalowane narzędzie git

#### Krok 1: Pobranie kodu źródłowego
1. Sklonuj repozytorium z plikami terraform:
```
git clone https://github.com/sejsmograf/spotspeak-terraform.git
```
2. Zainicjuj terraform:
```
cd spotspeak-terraform/app
terraform init
cd ../keycloak
terraform init
```

#### Krok 2: Stworzenie serwera Keycloak
1. Będąc w katalogu keycloak:
```
terraform apply
```
Będziesz musiał podać ściężkę do klucza publicznego używanego do dostępu do maszyn EC2.

2. Zaczekaj na zakończenie procesu. Po zakończeniu procesu, wyświetlony zostanie adres IP serwera Keycloak.
3. Dostań się do miejsca w którym przechowywany jest klucz prywatny używany do dostępu do maszyn EC2.
Następnie wykonaj poniższą komendę:
```
ssh -i <ścieżka_do_klucza_prywatnego> ubuntu@<adres_ip_keycloak>
```
#### Krok 3: Skonfigurowanie nginx jako reverse proxy z HTTPS dla Keycloak
1. Wygeneruj certyfikat SSL:
```
sudo certbot certonly --nginx
```
Zapytany o domenę wpisz domenę, pod którą będzie dostępny serwer Keycloak. 
Ważne jest aby domena wskaźnikowała na adres IP serwera Keycloak.

2. Skonfiguruj nginx:
```
sudo vim /etc/nginx/sites-available/default
```
Zamień zawartość pliku na:
```
server {
    listen 80;
    server_name <domena_keycloak>;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name <domena_keycloak>;

    ssl_certificate /etc/letsencrypt/live/<domena_keycloak>/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/<domena_keycloak>/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
3. Zrestartuj nginx:
```
sudo systemctl restart nginx
```

#### Krok 4: Skonfigurowanie Keycloak
1. Utwórz plik .env ze zmiennymi środowiskowymi:
```
cd /home/ubuntu/keycloak
vim .env
```
W pliku .env umieść następujące zmienne środowiskowe:
```
KEYCLOAK_ADMIN=<login_admina>
KEYCLOAK_ADMIN_PASSWORD=<hasło_admina>
INSERT_USER_ENDPOINT=https://<nazwa_drugiej_domeny>/api/users/init
```
2. Uruchom server Keycloak:
```
docker compose up -d
```
3. Za pomocą przeglądarki internetowej wejdź na nazwę domeny wskazującą na serwer Keycloak.
Zaloguj się na konto admina używając danych podanych w pliku .env.

4. Zaimportuj realm z pliku realm-export.json znajdującego się w katalogu keycloak.

Keycloak dropdown -> Create realm -> Browse -> Wybierz realm-export.json -> Create

5. Wygeneruj nowe klucze klientów

Client -> flutter-frontend -> Credentials -> Regenerate Secret
Client -> spring-backend -> Credentials -> Regenerate Secret
Zapisz gdzieś nowe klucze. Będziesz ich potrzebował w dalszej części wdrożenia.

6. Serwer Keycloak powinien być gotowy do użycia

#### Krok 5. Stworzenie serwera aplikacyjnego
1. Dostań się do katalogu spotspeak-terraform/app

2. Uruchom terraform:
```
terraform apply
```
Tym razem będzie trzeba podać więcej zmiennych środowiskowych. 
Ich nazwy jasno wskazują co powinno być w nich zawarte.
Ważny będzie sekret Keycloak utworzony w poprzedniym kroku.

3. Po zakończeniu procesu, wyświetlony zostanie adres IP serwera aplikacyjnego oraz 
adres bazy danych. Będziesz ich potrzebował w dalszej części wdrożenia.

4. Dostań się do miejsca w którym przechowywany jest klucz prywatny używany do dostępu do maszyn EC2.
Następnie wykonaj poniższą komendę:
```
ssh -i <ścieżka_do_klucza_prywatnego> ubuntu@<adres_ip_servera>
```

5. Wypisz zmienne odpowiadające za bazę danych i dostań się do bazy danych:
```
echo $ENV_SPRING_DATASOURCE_USERNAME
echo $ENV_SPRING_DATASOURCE_PASSWORD
psql -h <adres_bazy_danych> -U $ENV_SPRING_DATASOURCE_USERNAME -d postgres
```
Zapytany o hasło wpisz hasło z zmiennej $ENV_SPRING_DATASOURCE_PASSWORD.
Następnie wykonaj poniższe zapytanie:
```
CREATE EXTENSION postgis;
```


6. Utwórz certyfikat SSL oraz skonfiguruj nginx dokładnie tak jak w kroku 3 
(tym razem zamiast domeny Keycloak użyj domeny serwera aplikacyjnego, wskazującej na adres IP servera).

7. Uzyskaj plik z poświadczeniami .json z projektu Firebase i umieść go /home/ubuntu/spotspeak/firebase.json

8. Uruchom serwer aplikacyjny:
```
cd /home/ubuntu/spotspeak
./build_docker.sh --full-build
./run_docker.sh
```
