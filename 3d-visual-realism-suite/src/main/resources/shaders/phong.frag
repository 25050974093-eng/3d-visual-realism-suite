#version 330 core
out vec4 FragColor;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;
in vec4 FragPosLightSpace;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

uniform sampler2D shadowMap;

uniform bool useBlinnPhong;
uniform bool useFlatShading;

float ShadowCalculation(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    
    if (projCoords.z > 1.0) return 0.0;
    
    float closestDepth = texture(shadowMap, projCoords.xy).r; 
    float currentDepth = projCoords.z;
    
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
    float shadow = currentDepth - bias > closestDepth ? 0.6 : 0.0;

    return shadow;
}

// Menghitung Normal Per-Face untuk Flat Shading (Kompatibel Semua Driver)
vec3 getFlatNormal(vec3 pos) {
    vec3 dX = vec3(dFdx(pos.x), dFdx(pos.y), dFdx(pos.z));
    vec3 dY = vec3(dFdy(pos.x), dFdy(pos.y), dFdy(pos.z));
    return normalize(cross(dX, dY));
}

void main() {
    // Indikator 2: Flat Shading (Per-Face) vs Smooth Shading (Per-Vertex)
    vec3 norm = useFlatShading ? getFlatNormal(FragPos) : normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    
    // 1. Ambient Lighting
    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * lightColor;
    
    // 2. Diffuse Lighting (Lambertian)
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;
    
    // 3. Specular Lighting (Indikator 1: Phong vs Blinn-Phong)
    float specularStrength = 3.0; // Ditingkatkan agar kilauan putih sangat jelas
    vec3 viewDir = normalize(viewPos - FragPos);
    float spec = 0.0;
    
    if (useBlinnPhong) {
        // Blinn-Phong: Kilauan putih SANGAT FOKUS & KECIL (Exponent 128)
        vec3 halfwayDir = normalize(lightDir + viewDir);
        spec = pow(max(dot(norm, halfwayDir), 0.0), 128.0);
    } else {
        // Phong: Kilauan putih SANGAT LEBAR & MENYEBAR (Exponent 8)
        vec3 reflectDir = reflect(-lightDir, norm);
        spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
    }
    vec3 specular = specularStrength * spec * lightColor;
    
    // Indikator 5: Calculation Shadow Mapping
    float shadow = ShadowCalculation(FragPosLightSpace, norm, lightDir);
    vec3 lighting = (ambient + (1.0 - shadow) * (diffuse + specular));
    
    FragColor = vec4(lighting * objectColor, 1.0);
}