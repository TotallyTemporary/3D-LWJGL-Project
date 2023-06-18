#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 textureCoords;
layout (location = 2) in vec2 light;

out vec2 pass_light;
out vec2 pass_textureCoords;
flat out float pass_textureIndex;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main()
{
    pass_textureCoords = textureCoords.xy;
    pass_textureIndex  = textureCoords.z;
    pass_light = light;

    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(pos.xyz, 1.0);
}