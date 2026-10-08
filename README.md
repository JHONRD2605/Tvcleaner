# TV Cleaner — limpiador de memoria y gestor de apps para Android TV / TV Box

App enfocada para manejarse **solo con el control remoto**. Hace dos cosas:

1. **LIBERAR MEMORIA** — botón grande arriba. Pide al sistema que recupere
   RAM de apps inactivas y te muestra cuánta memoria libre quedó.
2. **Cerrar apps** — lista navegable con las flechas del control. Al elegir
   una app, se abre la pantalla oficial de Android donde pulsas
   *Forzar detención* para cerrarla de verdad.

Salir: botón **Atrás** del control.

---

## ⚠️ Lo que esta app SÍ y NO puede hacer (importante)

En Android 5 en adelante, **ninguna app sin root puede matar procesos de
otras apps por su cuenta**. Ese permiso solo lo tiene el sistema operativo.
Los "limpiadores" que prometen cerrar todo con un botón, o no lo hacen, o
piden permisos peligrosos y suelen traer publicidad o malware.

Esta app es honesta: usa las dos únicas vías legítimas (pedir al sistema que
libere RAM, y abrir la pantalla de *Forzar detención* de cada app). Es lo
máximo que se puede hacer de forma segura y sin root. Funciona igual en tu
**Onn (1.5 GB)** y en tu **Google TV (4 GB)**.

---

## Cómo obtener el APK

No incluye el APK ya compilado. Elige UNA de estas dos formas (ambas gratis):

### Opción A — GitHub Actions (recomendada, no instalas nada)

1. Crea un repositorio nuevo en tu cuenta de GitHub (ej. `tvcleaner`).
2. Sube todos estos archivos al repo (arrastrándolos en la web, o con git).
3. Entra a la pestaña **Actions** del repo. Verás el flujo *Compilar APK*.
   Si no corre solo, pulsa **Run workflow**.
4. Cuando termine (unos 2–3 min, ícono verde), entra a esa ejecución y
   descarga el artefacto **TvCleaner-apk**. Dentro está `app-debug.apk`.

### Opción B — Android Studio (en tu PC)

1. Instala Android Studio (gratis).
2. Abre esta carpeta como proyecto. Deja que descargue lo que pida.
3. Menú **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Cómo instalarlo en el TV Box

1. Pasa `app-debug.apk` al TV Box (por un USB, o con la app *Send Files to TV*,
   o descargándolo desde el navegador del propio TV Box).
2. En el TV Box, activa **Fuentes desconocidas** para el instalador que uses
   (Ajustes → Apps / Seguridad).
3. Abre el APK con un explorador de archivos y acepta instalar.
4. La app aparecerá en tu pantalla de inicio como **TV Cleaner**.

---

## Datos técnicos

- `minSdk 22` (Android 5.1+), `targetSdk 34`.
- Firmado con la clave de *debug*: instalable para uso personal, no apto para
  publicar en Play Store.
- Sin anuncios, sin rastreo, sin permisos de red. El código es todo lo que ves.
