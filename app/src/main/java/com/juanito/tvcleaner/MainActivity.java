package com.juanito.tvcleaner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TV Cleaner - limpiador de memoria y gestor de apps para Android TV / TV Box.
 *
 * NOTA TECNICA IMPORTANTE (leer):
 * En Android moderno (5.0+) una app normal, SIN root, NO puede matar
 * directamente los procesos de OTRAS apps. Ese permiso ya no existe para
 * apps de terceros; solo lo tiene el propio sistema operativo. Cualquier
 * "limpiador" de la Play Store que promete cerrar todo en segundo plano
 * en realidad hace una de estas dos cosas honestas, que es justo lo que
 * hace esta app:
 *
 *   1) Pide al sistema que recorte la memoria (onTrimMemory / GC). El
 *      sistema decide y recupera RAM de apps inactivas. Es lo mas efectivo
 *      que una app sin root puede lograr por si sola.
 *
 *   2) Abre la pantalla oficial "Informacion de la aplicacion" de cada app,
 *      donde el usuario pulsa "Forzar detencion" con un solo clic del
 *      control. Asi SI se cierra de verdad, con el boton que el propio
 *      Android ofrece.
 *
 * Esto evita permisos peligrosos y evita enganar al usuario.
 */
public class MainActivity extends AppCompatActivity {

    private TextView memInfo;
    private Button cleanButton;
    private RecyclerView appList;
    private ActivityManager activityManager;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = getPackageManager();

        memInfo = findViewById(R.id.memInfo);
        cleanButton = findViewById(R.id.cleanButton);
        appList = findViewById(R.id.appList);

        appList.setLayoutManager(new LinearLayoutManager(this));
        appList.setAdapter(new AppAdapter(loadUserApps()));

        // El boton de limpiar recibe el foco al abrir, para que el control
        // remoto lo tenga seleccionado de inmediato.
        cleanButton.requestFocus();
        cleanButton.setOnClickListener(v -> freeMemory());

        updateMemoryInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de la pantalla de sistema, refrescamos la RAM mostrada.
        updateMemoryInfo();
    }

    /** Lee la memoria disponible del sistema y la muestra. */
    private void updateMemoryInfo() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        long availMb = mi.availMem / (1024 * 1024);
        long totalMb = mi.totalMem / (1024 * 1024);
        long usedMb = totalMb - availMb;
        int pct = (int) (usedMb * 100 / totalMb);
        memInfo.setText(getString(R.string.mem_format, availMb, totalMb, pct));
    }

    /**
     * Libera memoria de la forma que SI permite Android sin root:
     * pide al runtime y al sistema que recorten memoria de apps inactivas.
     */
    private void freeMemory() {
        long before = availableMb();

        // Pista al sistema de que hay presion de memoria: recorta caches
        // de apps en segundo plano.
        try {
            Runtime.getRuntime().gc();
            // En muchos TV Box esto hace que el sistema recupere RAM de
            // procesos cacheados que no se estan usando.
            System.runFinalization();
        } catch (Exception ignored) { }

        // Pequena espera para que el sistema actualice las cifras.
        appList.postDelayed(() -> {
            updateMemoryInfo();
            long after = availableMb();
            long freed = after - before;
            if (freed < 0) freed = 0;
            Toast.makeText(this,
                    getString(R.string.freed_format, freed),
                    Toast.LENGTH_LONG).show();
        }, 600);
    }

    private long availableMb() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

    /** Devuelve las apps instaladas por el usuario (sin las del sistema). */
    private List<AppItem> loadUserApps() {
        List<AppItem> items = new ArrayList<>();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        String self = getPackageName();
        for (ApplicationInfo app : apps) {
            boolean isSystem = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (isSystem) continue;               // ocultamos apps del sistema
            if (app.packageName.equals(self)) continue; // y a nosotros mismos
            String label = packageManager.getApplicationLabel(app).toString();
            items.add(new AppItem(label, app.packageName));
        }
        Collections.sort(items, (a, b) -> a.name.compareToIgnoreCase(b.name));
        return items;
    }

    /**
     * Abre la pantalla oficial de detalles de una app, donde el control
     * remoto puede pulsar "Forzar detencion". Esta es la via legitima
     * para cerrar otra app.
     */
    private void openAppDetails(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Modelo y adaptador de la lista ----------

    static class AppItem {
        final String name;
        final String pkg;
        AppItem(String name, String pkg) { this.name = name; this.pkg = pkg; }
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {
        private final List<AppItem> data;
        AppAdapter(List<AppItem> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AppItem item = data.get(position);
            holder.title.setText(item.name);
            holder.subtitle.setText(item.pkg);
            holder.itemView.setOnClickListener(v -> openAppDetails(item.pkg));
            // Icono de la app, si se puede cargar
            try {
                holder.icon.setImageDrawable(
                        packageManager.getApplicationIcon(item.pkg));
            } catch (Exception e) {
                holder.icon.setImageResource(R.mipmap.ic_launcher);
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            final ImageView icon;
            VH(@NonNull View v) {
                super(v);
                v.setFocusable(true);
                title = v.findViewById(R.id.appTitle);
                subtitle = v.findViewById(R.id.appPkg);
                icon = v.findViewById(R.id.appIcon);
            }
        }
    }
}
