package mchorse.aperture.camera.fixtures;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.data.StructureBase;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.values.ValueModifiers;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueLong;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.network.IByteBufSerializable;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Abstract camera fixture
 *
 * Camera fixtures are the special types of class that store camera
 * transformations based on some variables.
 *
 * Every fixture have duration field.
 */
public abstract class AbstractFixture extends StructureBase implements IByteBufSerializable
{
    /**
     * The name of this camera fixture. Added just for organization.
     */
    public final ValueString name = new ValueString("name", "");

    /**
     * Custom color tint for fixtures.
     */
    public final ValueInt color = new ValueInt("color", 0x000000);

    /**
     * Duration of this fixture. Represented in ticks. There are 20 ticks in a
     * second.
     */
    public final ValueLong duration = new ValueLong("duration", 1, 1, Long.MAX_VALUE);

    /**
     * List of camera modifiers.
     */
    public final ValueModifiers modifiers = new ValueModifiers("modifiers");

    /**
     * Default constructor. All subclasses must implement the same 
     * constructor, because {@link FixtureRegistry} depends on it. 
     */
    public AbstractFixture(long duration)
    {
        this.setDuration(duration);

        this.register(this.name);
        this.register(this.color);
        this.register(this.duration);
        this.register(this.modifiers);
    }

    public IConfigValue getProperty(String name)
    {
        IConfigValue value = this.category.values.get(name);

        if (value == null && name.contains("."))
        {
            String[] splits = name.split("\\.");

            value = this.searchRecursively(this.category.values.get(splits[0]), splits, 0, name);
        }

        if (value == null)
        {
            throw new IllegalStateException("Property by name " + name + " can't be found!");
        }

        return value;
    }

    private IConfigValue searchRecursively(IConfigValue value, String[] splits, int i, String name)
    {
        if (value == null)
        {
            return null;
        }

        for (IConfigValue child : value.getSubValues())
        {
            if (child.getId().equals(name))
            {
                return child;
            }
            else if (i + 1 < splits.length && name.startsWith(child.getId()))
            {
                return this.searchRecursively(child, splits, i + 1, name);
            }
        }

        return null;
    }

    public void initiate()
    {}

    /* Duration management */

    /**
     * Set duration (duration is in milliseconds)
     */
    public void setDuration(long duration)
    {
        this.duration.set(duration);
    }

    /**
     * Get duration
     */
    public long getDuration()
    {
        return this.duration.get();
    }

    /**
     * Get some properties from player upon creation
     */
    public void fromPlayer(EntityPlayer player)
    {}

    /* Abstract methods */

    /**
     * Apply this fixture onto position with same preview partial tick
     */
    public void applyFixture(long ticks, float partialTick, CameraProfile profile, Position pos)
    {
        this.applyFixture(ticks, partialTick, partialTick, profile, pos);
    }

    /**
     * Apply this fixture onto position
     */
    public abstract void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos);

    /**
     * Apply this fixture onto position using a double between 0 and 1
     */
    public void applyFixture(double range, float partialTick, CameraProfile profile, Position pos)
    {
        long ticks = (long) (range * this.getDuration());

        this.applyFixture(ticks, partialTick, profile, pos);
    }

    /**
     * Apply this fixture onto position using a double between 0 and 1
     */
    public void applyLast(CameraProfile profile, Position pos)
    {
        this.applyFixture(this.getDuration(), 0, profile, pos);
    }

    /**
     * Before applying this fixture
     */
    public void preApplyFixture(long ticks, Position pos)
    {}

    /**
     * Get modifiers 
     */
    public List<AbstractModifier> getModifiers()
    {
        return this.modifiers.get();
    }

    /**
     * Clone this fixture
     */
    public final AbstractFixture copy()
    {
        AbstractFixture fixture = this.create(this.getDuration());

        fixture.copy(this);

        return fixture;
    }

    /**
     * Create new fixture
     */
    public abstract AbstractFixture create(long duration);

    /**
     * Copy data from another fixture during replacement
     */
    public void copyByReplacing(AbstractFixture from)
    {
        this.copy(from);
        this.setDuration(from.getDuration());
    }
}