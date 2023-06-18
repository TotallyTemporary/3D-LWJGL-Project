#version 330 core

in vec2 pass_textureCoords;
flat in float pass_textureIndex;
in vec2 pass_light;

out vec4 FragColor;

uniform sampler2DArray arrayTexture;

const float dayTime = 1.0;
const float lightGamma = 2.2;
const float minLight = 1.0 / 64;

void main()
{
    vec4 tex = texture(arrayTexture, vec3(pass_textureCoords.xy, pass_textureIndex));
    if (tex.a < 0.5f) {
      discard;
    }

    float sky = max(pass_light.y, minLight);

    float light = pow(sky * dayTime, 1.0/lightGamma);
    FragColor = vec4(tex.rgb * light, 1.0);
}