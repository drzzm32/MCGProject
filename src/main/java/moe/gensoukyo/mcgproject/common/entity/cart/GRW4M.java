package moe.gensoukyo.mcgproject.common.entity.cart;

import club.nsdn.nyasamarailway.api.cart.CartUtil;
import club.nsdn.nyasamarailway.block.BlockPlatform;
import club.nsdn.nyasamarailway.ext.AbsWireLoco;
import moe.gensoukyo.mcgproject.common.entity.MCGEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@MCGEntity("grw_4m")
public class GRW4M extends AbsWireLoco {

    public static void spawn(World world, double x, double y, double z, EnumFacing facing) {
        GRW4M head = new GRW4M(world, x, y, z);
        world.spawnEntity(head);

        Basket basket = new Basket(world, x, y + head.getDropDist(), z);
        basket.startRiding(head);
        world.spawnEntity(basket);
    }

    @MCGEntity("grw_4m_basket")
    public static class Basket extends AbsBasket {

        public Basket(World world) {
            super(world);
            this.setSize(3.5F, 3.0F);
        }

        public Basket(World world, double x, double y, double z) {
            super(world, x, y, z);
            this.setSize(3.5F, 3.0F);
        }

        @Override
        public boolean shouldSit() { return true; }

        @Override
        public double passengerYOffset() { return 0.4; }

        @Override
        public int getMaxPassengerSize() { return 4; }

        @Override
        public void passengerUpdate(@Nonnull Entity passenger) {
            double x = this.posX;
            double z = this.posZ;
            double y = this.posY + this.getMountedYOffset() + passenger.getYOffset();
            if (this.isPassenger(passenger)) {
                int index = this.getPassengers().indexOf(passenger);
                double distX = 1;
                double distZ = 0.75;
                double vx = distX * (double)(index / 2 == 1 ? 1 : -1);
                double vz = distZ * (double)(index % 3 == 0 ? 1 : -1);
                Vec3d vec = new Vec3d(vx, 0.0D, vz);
                vec = CartUtil.rotatePitchFix(vec, (float)((double)((this.rotationPitch + 360.0F) / 180.0F) * Math.PI));
                vec = vec.rotateYaw((float)((double)((180.0F - this.rotationYaw) / 180.0F) * Math.PI));
                passenger.setPosition(x + vec.x, y + vec.y, z + vec.z);
            }
        }

        public BlockPos prevPos = null;

        @Override
        public void update() {
            Entity entity = this.getRidingEntity();
            if (!(entity instanceof AbsWireLoco)) {
                if (prevPos != null)
                    removeLight(prevPos);
                BlockPos pos = getPosition();
                int dist = 2;
                pos = pos.add(0, dist, 0);
                removeLight(pos);
            } else {
                BlockPos pos = getPosition();
                int dist = 2;
                pos = pos.add(0, dist, 0);

                if (!pos.equals(prevPos)) {
                    if (prevPos != null)
                        removeLight(prevPos);
                    addLight(pos);
                    prevPos = new BlockPos(pos);
                }
            }
        }

        private void addLight(BlockPos pos) {
            this.world.setLightFor(EnumSkyBlock.BLOCK, pos, 15);
            Vec3i vec = new Vec3i(8, 8, 8);
            this.world.markBlockRangeForRenderUpdate(pos.subtract(vec), pos.add(vec));
            this.world.updateBlockTick(pos, this.world.getBlockState(pos).getBlock(), 1, 0);
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.up());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.down());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.north());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.south());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.west());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos.east());
        }

        private void removeLight(BlockPos pos) {
            this.world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        }

        @Override
        public void onKilled() {
            if (getRidingEntity() instanceof GRW4M)
                getRidingEntity().setDead();
        }

        @Override
        public BlockPos passengerRemove(@Nonnull Entity passenger) {
            BlockPos pos = this.getPosition();

            if (world.getBlockState(pos.north(2)).getBlock() instanceof BlockPlatform)
                pos = pos.north(3).up();
            else if (world.getBlockState(pos.south(2)).getBlock() instanceof BlockPlatform)
                pos = pos.south(3).up();
            else if (world.getBlockState(pos.west(2)).getBlock() instanceof BlockPlatform)
                pos = pos.west(3).up();
            else if (world.getBlockState(pos.east(2)).getBlock() instanceof BlockPlatform)
                pos = pos.east(3).up();
            else {
                pos = pos.down();
                if (world.getBlockState(pos.north(2)).getBlock() instanceof BlockPlatform)
                    pos = pos.north(3).up();
                else if (world.getBlockState(pos.south(2)).getBlock() instanceof BlockPlatform)
                    pos = pos.south(3).up();
                else if (world.getBlockState(pos.west(2)).getBlock() instanceof BlockPlatform)
                    pos = pos.west(3).up();
                else if (world.getBlockState(pos.east(2)).getBlock() instanceof BlockPlatform)
                    pos = pos.east(3).up();
                else
                    pos = null;
            }

            return pos;
        }

    }

    public GRW4M(World world) {
        super(world);
    }

    public GRW4M(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public double getDropDist() { return -4; }

    @Override
    public void onKilled() {
        for (Entity e : getPassengers()) {
            if (e instanceof Basket)
                e.setDead();
        }
    }

}
