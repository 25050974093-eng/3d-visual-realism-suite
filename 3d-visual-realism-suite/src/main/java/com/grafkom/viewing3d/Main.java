package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    private long window;
    private final int WIDTH = 1280, HEIGHT = 720;

    // State Kontrol Interaktif Modul (Minggu 11)
    private boolean useBlinnPhong = true;   // Key 'B'
    private boolean useFlatShading = false; // Key 'F'
    private boolean enableDepthTest = true; // Key 'Z'
    private boolean enableCulling = true;   // Key 'C'
    private boolean isRotating = true;      // Key 'SPACE'

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("Gagal menginisialisasi GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "3D Visual Realism Suite - Minggu 11", 0, 0);
        if (window == 0) throw new RuntimeException("Gagal membuat jendela GLFW");

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GL.createCapabilities();

        System.out.println("==================================================");
        System.out.println("3D VISUAL REALISM SUITE INITIALIZED (LWJGL 3 + GLSL)");
        System.out.println("KONTROL KEYBOARD INTERAKTIF (MODUL MINGGU 11):");
        System.out.println("  [B] Toggle Phong / Blinn-Phong Shading");
        System.out.println("  [F] Toggle Flat / Smooth Interpolation");
        System.out.println("  [Z] Toggle Z-Buffer Depth Test");
        System.out.println("  [C] Toggle Back-Face Culling");
        System.out.println("  [SPACE] Pause / Start Rotasi");
        System.out.println("==================================================");

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    if (key == GLFW_KEY_B) {
                        useBlinnPhong = !useBlinnPhong;
                        System.out.println("-> Mode Specular (Key B): " + (useBlinnPhong ? "Blinn-Phong (Pantulan Tajam & Fokus)" : "Phong (Pantulan Menyebar & Soft)"));
                    }
                    if (key == GLFW_KEY_F) {
                        useFlatShading = !useFlatShading;
                        System.out.println("-> Mode Interpolasi (Key F): " + (useFlatShading ? "Flat Shading (Objek Patah-Patah / Faceted)" : "Smooth Shading (Objek Bulat Mulus)"));
                    }
                    if (key == GLFW_KEY_Z) {
                        enableDepthTest = !enableDepthTest;
                        System.out.println("-> Z-Buffer Depth Test (Key Z): " + (enableDepthTest ? "AKTIF" : "NONAKTIF"));
                    }
                    if (key == GLFW_KEY_C) {
                        enableCulling = !enableCulling;
                        System.out.println("-> Back-Face Culling (Key C): " + (enableCulling ? "AKTIF" : "NONAKTIF"));
                    }
                    if (key == GLFW_KEY_SPACE) {
                        isRotating = !isRotating;
                        System.out.println("-> Status Rotasi: " + (isRotating ? "JALAN" : "PAUSE"));
                    }
                }
            }
        });
    }

    private void loop() {
        Shader shadowShader = new Shader("/shaders/shadowDepth.vert", "/shaders/shadowDepth.frag");
        Shader mainShader = new Shader("/shaders/phong.vert", "/shaders/phong.frag");

        Mesh sphereMesh = Mesh.createSphere(16, 16);
        Mesh planeMesh = Mesh.createPlane();

        ShadowMapFBO shadowFBO = new ShadowMapFBO();

        Vector3f lightPos = new Vector3f(2.0f, 4.0f, 2.0f);
        Vector3f viewPos = new Vector3f(0.0f, 3.0f, 6.0f);

        float rotation = 0.0f;

        while (!glfwWindowShouldClose(window)) {
            if (isRotating) {
                rotation += 0.01f;
            }

            // Indikator 3: Hidden Surface Removal Controls
            HSRManager.setupDepthTest(enableDepthTest);
            HSRManager.setupBackFaceCulling(enableCulling);

            Matrix4f lightProjection = new Matrix4f().ortho(-10.0f, 10.0f, -10.0f, 10.0f, 1.0f, 15.0f);
            Matrix4f lightView = new Matrix4f().lookAt(lightPos, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
            Matrix4f lightSpaceMatrix = new Matrix4f(lightProjection).mul(lightView);

            Matrix4f sphereModel = new Matrix4f().translate(0.0f, 1.0f, 0.0f).rotate(rotation, 0.0f, 1.0f, 0.0f);
            Matrix4f planeModel = new Matrix4f().translate(0.0f, 0.0f, 0.0f);

            // Pass 1: Indikator 5 (Shadow Depth Pass)
            shadowFBO.bindForWriting();
            shadowShader.use();
            shadowShader.setMat4("lightSpaceMatrix", lightSpaceMatrix);

            shadowShader.setMat4("model", sphereModel);
            sphereMesh.render();

            shadowShader.setMat4("model", planeModel);
            planeMesh.render();

            shadowFBO.unbind(WIDTH, HEIGHT);

            // Pass 2: Main Render Pass
            glDrawBuffer(GL_BACK);
            glReadBuffer(GL_BACK);
            glClearColor(0.12f, 0.12f, 0.18f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            mainShader.use();

            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0), (float) WIDTH / HEIGHT, 0.1f, 100.0f);
            Matrix4f view = new Matrix4f().lookAt(viewPos, new Vector3f(0.0f, 0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));

            mainShader.setMat4("projection", projection);
            mainShader.setMat4("view", view);
            mainShader.setMat4("lightSpaceMatrix", lightSpaceMatrix);

            mainShader.setVec3("lightPos", lightPos);
            mainShader.setVec3("viewPos", viewPos);
            mainShader.setVec3("lightColor", new Vector3f(1.0f, 1.0f, 1.0f));

            mainShader.setBool("useBlinnPhong", useBlinnPhong);
            mainShader.setBool("useFlatShading", useFlatShading);

            shadowFBO.bindDepthTexture(1);
            mainShader.setInt("shadowMap", 1);

            // Render Objek Utama (Bola 3D)
            mainShader.setMat4("model", sphereModel);
            mainShader.setVec3("objectColor", new Vector3f(0.2f, 0.7f, 0.9f));
            sphereMesh.render();

            // Render Bidang Lantai (Ground Plane)
            mainShader.setMat4("model", planeModel);
            mainShader.setVec3("objectColor", new Vector3f(0.6f, 0.6f, 0.6f));
            planeMesh.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}