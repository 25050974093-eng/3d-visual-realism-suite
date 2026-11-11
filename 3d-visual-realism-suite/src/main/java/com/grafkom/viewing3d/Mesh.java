package com.grafkom.viewing3d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private final int vaoID;
    private final int vboID;
    private final int eboID;
    private final int vertexCount;

    public Mesh(float[] vertices, int[] indices) {
        vertexCount = indices.length;
        vaoID = glGenVertexArrays();
        vboID = glGenBuffers();
        eboID = glGenBuffers();

        glBindVertexArray(vaoID);

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoID);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Objek Bola 3D (16 Stack & Sector) Ideal Untuk Menguji Flat vs Smooth Shading
    public static Mesh createSphere(int sectorCount, int stackCount) {
        List<Float> verticesList = new ArrayList<>();
        List<Integer> indicesList = new ArrayList<>();

        float radius = 0.85f;
        float sectorStep = (float) (2 * Math.PI / sectorCount);
        float stackStep = (float) (Math.PI / stackCount);

        for (int i = 0; i <= stackCount; ++i) {
            float stackAngle = (float) (Math.PI / 2 - i * stackStep);
            float xy = (float) (radius * Math.cos(stackAngle));
            float z = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= sectorCount; ++j) {
                float sectorAngle = j * sectorStep;
                float x = (float) (xy * Math.cos(sectorAngle));
                float y = (float) (xy * Math.sin(sectorAngle));

                // Position
                verticesList.add(x);
                verticesList.add(z);
                verticesList.add(y);

                // Normal
                float lengthInv = 1.0f / radius;
                verticesList.add(x * lengthInv);
                verticesList.add(z * lengthInv);
                verticesList.add(y * lengthInv);

                // TexCoords UV
                verticesList.add((float) j / sectorCount);
                verticesList.add((float) i / stackCount);
            }
        }

        for (int i = 0; i < stackCount; ++i) {
            int k1 = i * (sectorCount + 1);
            int k2 = k1 + sectorCount + 1;

            for (int j = 0; j < sectorCount; ++j, ++k1, ++k2) {
                if (i != 0) {
                    indicesList.add(k1);
                    indicesList.add(k2);
                    indicesList.add(k1 + 1);
                }
                if (i != (stackCount - 1)) {
                    indicesList.add(k1 + 1);
                    indicesList.add(k2);
                    indicesList.add(k2 + 1);
                }
            }
        }

        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) vertices[i] = verticesList.get(i);

        int[] indices = new int[indicesList.size()];
        for (int i = 0; i < indicesList.size(); i++) indices[i] = indicesList.get(i);

        return new Mesh(vertices, indices);
    }

    public static Mesh createPlane() {
        float[] vertices = {
            -5.0f, 0.0f, -5.0f,  0.0f, 1.0f, 0.0f,  0.0f, 5.0f,
             5.0f, 0.0f, -5.0f,  0.0f, 1.0f, 0.0f,  5.0f, 5.0f,
             5.0f, 0.0f,  5.0f,  0.0f, 1.0f, 0.0f,  5.0f, 0.0f,
            -5.0f, 0.0f,  5.0f,  0.0f, 1.0f, 0.0f,  0.0f, 0.0f
        };

        // Indeks CCW Presisi
        int[] indices = { 0, 3, 2, 2, 1, 0 };
        return new Mesh(vertices, indices);
    }
}