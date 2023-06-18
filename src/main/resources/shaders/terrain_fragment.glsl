#version 330 core

in vec2 pass_textureCoords;
flat in float pass_textureIndex;
in vec2 pass_light;

out vec4 FragColor;

uniform sampler2DArray arrayTexture;

const float dayTime = 1.0;

void main()
{
    vec4 tex = texture(arrayTexture, vec3(pass_textureCoords.xy, pass_textureIndex));
    if (tex.a < 0.5f) {
      discard;
    }

    float sky = pass_light.y;

    FragColor = vec4(tex.rgb * sky * dayTime, 1.0);
}