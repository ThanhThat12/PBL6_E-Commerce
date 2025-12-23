package com.PBL6.Ecommerce.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.Base64;

@Component
public class FirebaseConfig {
  
  // Constructor logging to verify Spring loads this bean
  public FirebaseConfig() {
    System.out.println("============================================");
    System.out.println("FirebaseConfig constructor called - Bean is being created");
    System.out.println("============================================");
  }
  
  @PostConstruct
  public void init() {
    System.out.println("============================================");
    System.out.println("FirebaseConfig @PostConstruct starting...");
    System.out.println("============================================");
    try (InputStream svc = resolveServiceAccountStream()) {
      if (svc == null) { 
        System.out.println("ERROR: Firebase creds missing; FCM disabled."); 
        return; 
      }
      System.out.println("SUCCESS: Firebase credentials loaded successfully");
      FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(svc))
        .build();
      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
        System.out.println("SUCCESS: Firebase initialized successfully!");
      } else {
        System.out.println("INFO: Firebase already initialized");
      }
    } catch (Exception e) { 
      System.err.println("ERROR: Firebase init failed: " + e.getMessage()); 
      e.printStackTrace();
    }
  }
  private InputStream resolveServiceAccountStream() throws IOException {
    String p = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    String b64 = System.getenv("FIREBASE_SA_B64");
    String json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

    System.out.println("🔍 Resolving Firebase credentials...");
    System.out.println("   GOOGLE_APPLICATION_CREDENTIALS: " + (p != null ? p : "not set"));
    System.out.println("   FIREBASE_SA_B64: " + (b64 != null ? "set (hidden)" : "not set"));
    System.out.println("   FIREBASE_SERVICE_ACCOUNT_JSON: " + (json != null ? "set (hidden)" : "not set"));

    if (p != null && !p.isBlank()) {
      System.out.println("✅ Using credentials from file: " + p);
      return new FileInputStream(p);
    }
    if (b64 != null && !b64.isBlank()) {
      System.out.println("✅ Using credentials from base64 env var");
      return new ByteArrayInputStream(Base64.getDecoder().decode(b64));
    }
    if (json != null && !json.isBlank()) {
      System.out.println("✅ Using credentials from FIREBASE_SERVICE_ACCOUNT_JSON env var");
      // Ghi ra file tạm
      String tmpPath = System.getProperty("os.name").toLowerCase().contains("win") ? "C:\\tmp\\firebase-service-account.json" : "/tmp/firebase-service-account.json";
      File tmpFile = new File(tmpPath);
      tmpFile.getParentFile().mkdirs();
      try (FileWriter fw = new FileWriter(tmpFile)) {
        fw.write(json);
      }
      System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", tmpPath);
      return new FileInputStream(tmpFile);
    }

    ClassPathResource r = new ClassPathResource("firebase-service-account.json");
    if (r.exists()) {
      System.out.println("✅ Using credentials from classpath");
      return r.getInputStream();
    }

    System.out.println("❌ No Firebase credentials found anywhere!");
    return null;
  }
}