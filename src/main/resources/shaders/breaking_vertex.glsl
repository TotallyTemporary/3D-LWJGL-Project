#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 textureCoords;

out vec3 pass_textureCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform int textureIndex;

void main()
{
    pass_textureCoords = vec3(textureCoords, textureIndex);
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(pos.xyz, 1.0);
}