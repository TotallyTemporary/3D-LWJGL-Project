#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 textureCoords;

out vec2 pass_textureCoords;
flat out float pass_textureIndex;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main()
{
    pass_textureCoords = textureCoords.xy;
    pass_textureIndex  = textureCoords.z;

    vec3 movedPos = vec3(pos.x - 0.5, pos.y, pos.z - 0.5);
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(movedPos, 1.0);
}