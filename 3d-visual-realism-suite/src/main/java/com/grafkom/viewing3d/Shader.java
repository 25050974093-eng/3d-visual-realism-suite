package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programID;

    public Shader(String vertPath, String fragPath) {
        String vertCode = loadSource(vertPath);
        String fragCode = loadSource(fragPath);

        int vertID = compileShader(vertCode, GL_VERTEX_SHADER);
        int fragID = compileShader(fragCode, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertID);
        glAttachShader(programID, fragID);
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking shader: " + glGetProgramInfoLog(programID));
        }

        glDeleteShader(vertID);
        glDeleteShader(fragID);
    }

    private String loadSource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Shader file not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader: " + path, e);
        }
    }

    private int compileShader(String code, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compilation error (" + type + "): " + glGetShaderInfoLog(id));
        }
        return id;
    }

    public void use() {
        glUseProgram(programID);
    }

    public void setMat4(String name, Matrix4f mat) {
        int loc = glGetUniformLocation(programID, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            mat.get(buffer);
            glUniformMatrix4fv(loc, false, buffer);
        }
    }

    public void setVec3(String name, Vector3f vec) {
        int loc = glGetUniformLocation(programID, name);
        glUniform3f(loc, vec.x, vec.y, vec.z);
    }

    public void setInt(String name, int value) {
        int loc = glGetUniformLocation(programID, name);
        glUniform1i(loc, value);
    }

    public void setBool(String name, boolean value) {
        int loc = glGetUniformLocation(programID, name);
        glUniform1i(loc, value ? 1 : 0);
    }
}