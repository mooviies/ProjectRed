package mrtjp.projectred.core.blockutil;

import java.util.ArrayList;

import mrtjp.projectred.core.BasicUtils;
import mrtjp.projectred.core.CoreSPH;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketTile;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;

public abstract class TileMulti extends TileEntity implements ICustomPacketTile
{
    protected long schedTick = -1L;

    public void onBlockNeighborChange(int l)
    {
    }

    public void onBlockPlaced(ItemStack ist, int side, EntityPlayer ent)
    {
    }

    public void onBlockRemoval()
    {
    }

    public int isBlockStrongPoweringTo(int side)
    {
        return 0;
    }

    public int isBlockWeakPoweringTo(int side)
    {
        return isBlockStrongPoweringTo(side);
    }

    public int getLightValue()
    {
        return 0;
    }

    public boolean isFireSource(ForgeDirection side)
    {
        return false;
    }

    public boolean isBlockSolidOnSide(ForgeDirection side)
    {
        return true;
    }

    public boolean onBlockActivated(EntityPlayer player)
    {
        return false;
    }

    public void onEntityCollidedWithBlock(Entity ent)
    {
    }

    public AxisAlignedBB getCollisionBoundingBox()
    {
        return null;
    }

    public void onScheduledTick()
    {
    }

    public void onTileTick(boolean client)
    {
    }

    public abstract int getBlockID();

    public int getMetaData()
    {
        return 0;
    }

    public void addHarvestContents(ArrayList<ItemStack> ist)
    {
        ist.add(new ItemStack(getBlockID(), 1, getMetaData()));
    }

    public void scheduleTick(int time)
    {
        long tn = worldObj.getTotalWorldTime() + time;
        if (schedTick > 0L && schedTick < tn)
            return;
        schedTick = tn;
        markDirty();
    }

    public boolean isTickScheduled()
    {
        return schedTick >= 0L;
    }

    public void breakBlock()
    {
        ArrayList<ItemStack> il = new ArrayList<ItemStack>();
        addHarvestContents(il);

        for (ItemStack it : il)
            BasicUtils.dropItem(worldObj, xCoord, yCoord, zCoord, it);

        worldObj.setBlock(xCoord, yCoord, zCoord, 0);
    }

    public final void markDirty()
    {
        BasicUtils.markBlockDirty(worldObj, xCoord, yCoord, zCoord);
    }

    public final void markRender()
    {
        worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public final void updateEntity()
    {
        if (worldObj.isRemote)
        {
            onTileTick(true);
            return;
        }
        else
            onTileTick(false);

        if (schedTick < 0L)
            return;

        long time = worldObj.getTotalWorldTime();
        if (schedTick <= time)
        {
            schedTick = -1L;
            onScheduledTick();
            markDirty();
        }
    }

    public final void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        schedTick = tag.getLong("sched");
        load(tag);
    }

    public final void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setLong("sched", schedTick);
        save(tag);
    }

    public void save(NBTTagCompound tag)
    {
    }

    public void load(NBTTagCompound tag)
    {
    }

    @Override
    public final Packet getDescriptionPacket()
    {
        PacketCustom packet = writeStream(0);
        writeDesc(packet);
        return packet.toPacket();
    }

    @Override
    public final void handleDescriptionPacket(PacketCustom packet)
    {
        int switchkey = packet.readUByte();
        if (switchkey == 0)
            readDesc(packet);
        else
            read(packet, switchkey);
    }

    public void read(MCDataInput in, int switchkey)
    {
    }

    public void readDesc(MCDataInput in)
    {
    }

    public void writeDesc(MCDataOutput out)
    {
    }

    public PacketCustom writeStream(int switchkey)
    {
        PacketCustom stream = new PacketCustom(CoreSPH.channel, 1);
        stream.writeCoord(new BlockCoord(this)).writeByte(switchkey);
        return stream;
    }

    public int resolveLook(EntityPlayer player)
    {
        int rot = (int) Math.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
        if (Math.abs(player.posX - xCoord) < 2D && Math.abs(player.posZ - zCoord) < 2D)
        {
            double p = player.posY + 1.82D - player.yOffset - this.yCoord;
            if (p > 2.0D)
                return 0;
            if (p < 0.0D)
                return 1;
        }
        switch (rot) {
        case 0:
            return 3;
        case 1:
            return 4;
        case 2:
            return 2;
        case 3:
            return 5;
        }
        return 1;
    }
}