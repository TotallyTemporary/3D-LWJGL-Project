#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 textureCoords;

out vec3 pass_textureCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main()
{
    pass_textureCoords = textureCoords;

    // transform the model back so its center is at 0,0,0.
    vec4 movedOrigin = vec4(pos.x - 0.5f, pos.y - 0.5f, pos.z - 0.5f, 1.0);

    // transformation matrix has a +0.5f offset to move the model back.
    // the move is only so scaling happens along the appropriate origin.
    vec4 transformed = transformationMatrix * movedOrigin;

    gl_Position = projectionMatrix * viewMatrix * transformed;
}