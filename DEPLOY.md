## Instrukcja wdrożenia backendu

### Wstęp

Architektura backendu składa się z następujących części:
 - serwer aplikacyjny (Spring Boot)
 - serwer uwierzytelniający (Keycloak)
 - serwer bazodanowy (PostgreSQL z rozszerzeniem PostGIS, uruchomiony w usłudze Amazon RDS)
 - usługa przechowywania plików w chmurze (Amazon S3)
 - usługa CDN (Amazon CloudFront)
 - API dające dostęp dużego modelu językowego, kompatybilnego z OpenAI API (Używany jest Groq API)
 - FirebaseCloudMessaging do wysyłania powiadomień push

W związku ze stosunkowo skomplikowaną architekturą, zdecydowano się na wykorzystanie narzędzi
ułatwiających ten proces. Zautomatyzowano proces tworzenia architekruy chmurowej za pomocą Terraform.
Użyto również Docker oraz Docker Compose do zautomatyzowania procesu budowania i uruchamiania kontenerów.
Pozwoliło to w znacznym stopniu zautomatyzować proces wdrożenia oraz zarządzania infrastrukturą.

Warto jednak zaznaczyć, że wdrożenie nie jest w pełni zautomatyzowane. Manualny charakter
niektórych kroków (np. konfiguracja Keycloak) sprawia, że wdrożenie wymaga pewnej wiedzy
z zakresu zarządzania infrastrukturą oraz konfiguracji serwerów. Poniżej znajduje się
szczegółowa instrukcja wdrożenia.


### Wymagania wstępne
 - Konto w usłudze Amazon Web Services, posiadające dostęp do usług RDS, S3, CloudFront, EC2, IAM
 - Zainstalowane narzędzie Terraform, skonfigurowane z dostępem do powyższego konta
 - Dostęp do przynajmniej jednej nazwy domeny, wraz z możliwością zarządzania rekordami DNS
 - Konto w usłudze OpenAI, Groq lub innej kompatybilnej z dostępem do API
 - Dostęp do projektu Firebase aplikacji mobilnej, z dostępem do Firebase Cloud Messaging
 - Zainstalowane narzędzie git

#### Krok 1: Pobranie kodu źródłowego
1. Sklonuj repozytorium z plikami terraform:
```
git clone https://github.com/sejsmograf/spotspeak-terraform.git
```
